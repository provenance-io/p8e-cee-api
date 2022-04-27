package io.provenance.onboarding.frameworks.provenance

import com.google.common.io.BaseEncoding
import com.google.protobuf.Any
import cosmos.base.abci.v1beta1.Abci
import cosmos.tx.v1beta1.ServiceOuterClass
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.client.grpc.BaseReqSigner
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import io.provenance.client.grpc.Signer
import io.provenance.hdwallet.wallet.Account
import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.common.model.ProvenanceConfig
import io.provenance.onboarding.domain.usecase.common.model.TxBody
import io.provenance.onboarding.domain.usecase.common.model.TxResponse
import io.provenance.onboarding.frameworks.provenance.exceptions.ContractExceptionException
import io.provenance.onboarding.frameworks.provenance.extensions.getBaseAccount
import io.provenance.onboarding.frameworks.provenance.extensions.getCurrentHeight
import io.provenance.onboarding.frameworks.provenance.extensions.getErrorResult
import io.provenance.onboarding.frameworks.provenance.extensions.isError
import io.provenance.onboarding.frameworks.provenance.utility.ProvenanceUtils
import io.provenance.scope.sdk.Session
import io.provenance.scope.sdk.SignedResult
import org.springframework.stereotype.Component
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import mu.KotlinLogging

class ProvenanceTxException(message: String) : Exception(message)

sealed class ProvenanceTx
class SingleTx(val value: SignedResult) : ProvenanceTx()
class BatchTx(val value: Collection<SignedResult>) : ProvenanceTx()

@Component
class ProvenanceService : Provenance {

    private val log = KotlinLogging.logger { }
    private val cachedSequenceMap = ConcurrentHashMap<String, CachedAccountSequence>()

    override fun executeTransaction(config: ProvenanceConfig, session: Session, tx: ProvenanceTx, signer: Signer): Abci.TxResponse {
        val pbClient = PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION)

        return when (tx) {
            is SingleTx -> {
                when (val error = tx.getErrorResult()) {
                    null -> {
                        log.info("[L: ${session.scopeUuid}, S: ${session.sessionUuid}] Building the tx.")
                        val messages = tx.value.messages.map { Any.pack(it, "") }
                        val txBody = TxOuterClass.TxBody.newBuilder()
                            .setTimeoutHeight(getCurrentHeight(pbClient) + 12L)
                            .addAllMessages(messages)
                            .build()

                        log.info("[L: ${session.scopeUuid}, S: ${session.sessionUuid}] Determining account information for the tx.")
                        val cachedOffset = cachedSequenceMap.getOrPut(signer.address()) { CachedAccountSequence() }
                        val account = getBaseAccount(pbClient, signer.address())
                        val baseSigner = BaseReqSigner(
                            signer,
                            account = account,
                            sequenceOffset = cachedOffset.getAndIncrementOffset(account.sequence)
                        )

                        log.info("[L: ${session.scopeUuid}, S: ${session.sessionUuid}] Sending tx.")
                        val result = pbClient.estimateAndBroadcastTx(
                            txBody = txBody,
                            signers = listOf(baseSigner),
                            gasAdjustment = config.gasAdjustment,
                            mode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_BLOCK
                        )

                        if (result.isError()) {
                            cachedOffset.getAndDecrement(account.sequence)
                            throw ProvenanceTxException(result.txResponse.toString())
                        }
                        result.txResponse
                    }
                    else -> throw ContractExceptionException(error.result.errorMessage)
                }
            }
            is BatchTx -> throw IllegalArgumentException("Batched transactions are not supported.")
        }
    }

    override fun onboard(chainId: String, nodeEndpoint: String, account: Account, storeTxBody: TxBody): TxResponse {
        val pbClient = PbClient(chainId, URI(nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION)
        val utility = ProvenanceUtils()

        val txBody = TxOuterClass.TxBody.newBuilder().also {
            storeTxBody.base64.forEach { tx ->
                it.addMessages(Any.parseFrom(BaseEncoding.base64().decode(tx)))
            }
        }.build()

        val response = pbClient.estimateAndBroadcastTx(
            txBody = txBody,
            signers = listOf(BaseReqSigner(utility.getSigner(account))),
            mode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_SYNC,
            gasAdjustment = 1.5
        ).txResponse

        return TxResponse(response.txhash, response.gasWanted.toString(), response.gasUsed.toString(), response.height.toString())
    }
}
