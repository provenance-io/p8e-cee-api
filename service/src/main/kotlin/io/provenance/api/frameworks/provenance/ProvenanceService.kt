package io.provenance.api.frameworks.provenance

import com.google.common.io.BaseEncoding
import com.google.protobuf.Any
import cosmos.base.abci.v1beta1.Abci
import cosmos.tx.v1beta1.ServiceOuterClass
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.TxResponse
import io.provenance.client.grpc.BaseReqSigner
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import io.provenance.client.grpc.Signer
import io.provenance.metadata.v1.ScopeRequest
import io.provenance.metadata.v1.ScopeResponse
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.models.p8e.TxBody
import io.provenance.api.frameworks.provenance.exceptions.ContractTxException
import io.provenance.api.frameworks.provenance.extensions.getBaseAccount
import io.provenance.api.frameworks.provenance.extensions.getCurrentHeight
import io.provenance.api.frameworks.provenance.extensions.getErrorResult
import io.provenance.api.frameworks.provenance.extensions.isError
import io.provenance.scope.sdk.SignedResult
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.net.URI
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ProvenanceTxException(message: String) : Exception(message)

sealed class ProvenanceTx
class SingleTx(val value: SignedResult) : ProvenanceTx()
class BatchTx(val value: Collection<SignedResult>) : ProvenanceTx()

@Component
class ProvenanceService : Provenance {

    private val log = KotlinLogging.logger { }
    private val cachedSequenceMap = ConcurrentHashMap<String, CachedAccountSequence>()

    override fun buildContractTx(config: ProvenanceConfig, tx: ProvenanceTx): TxOuterClass.TxBody? =
        PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).use { pbClient ->
            return when (tx) {
                is SingleTx -> {
                    when (val error = tx.getErrorResult()) {
                        null -> {
                            log.info("Building the tx.")
                            val messages = tx.value.messages.map { Any.pack(it, "") }

                            TxOuterClass.TxBody.newBuilder()
                                .setTimeoutHeight(getCurrentHeight(pbClient) + 12L)
                                .addAllMessages(messages)
                                .build()
                        }
                        else -> throw ContractTxException(error.result.errorMessage)
                    }
                }
                is BatchTx -> throw IllegalArgumentException("Batched transactions are not supported.")
            }
        }

    override fun executeTransaction(config: ProvenanceConfig, tx: TxOuterClass.TxBody, signer: Signer): Abci.TxResponse {
        val pbClient = PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION)

        log.info("Determining account information for the tx.")
        val cachedOffset = cachedSequenceMap.getOrPut(signer.address()) { CachedAccountSequence() }
        val account = getBaseAccount(pbClient, signer.address())
        val baseSigner = BaseReqSigner(
            signer,
            account = account,
            sequenceOffset = cachedOffset.getAndIncrementOffset(account.sequence)
        )

        log.info("Sending tx.")
        val result = pbClient.estimateAndBroadcastTx(
            txBody = tx,
            signers = listOf(baseSigner),
            gasAdjustment = config.gasAdjustment,
            mode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_BLOCK
        )
        pbClient.close()

        if (result.isError()) {
            cachedOffset.getAndDecrement(account.sequence)
            throw ProvenanceTxException(result.txResponse.toString())
        }

        return result.txResponse
    }

    override fun onboard(chainId: String, nodeEndpoint: String, signer: Signer, storeTxBody: TxBody): TxResponse {
        val pbClient = PbClient(chainId, URI(nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION)

        val txBody = TxOuterClass.TxBody.newBuilder().also {
            storeTxBody.base64.forEach { tx ->
                it.addMessages(Any.parseFrom(BaseEncoding.base64().decode(tx)))
            }
        }.build()

        val response = pbClient.estimateAndBroadcastTx(
            txBody = txBody,
            signers = listOf(BaseReqSigner(signer)),
            mode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_SYNC,
            gasAdjustment = 1.5
        ).txResponse

        pbClient.close()

        return TxResponse(response.txhash, response.gasWanted.toString(), response.gasUsed.toString(), response.height.toString())
    }

    override fun getScope(config: ProvenanceConfig, scopeUuid: UUID): ScopeResponse =
        PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).use { pbClient ->
            pbClient.metadataClient.scope(
                ScopeRequest.newBuilder()
                    .setScopeId(scopeUuid.toString())
                    .setIncludeRecords(true)
                    .setIncludeSessions(true)
                    .build()
            )
        }
}
