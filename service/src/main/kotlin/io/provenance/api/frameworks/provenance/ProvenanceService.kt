package io.provenance.api.frameworks.provenance

import com.google.protobuf.Any
import cosmos.auth.v1beta1.Auth
import cosmos.base.abci.v1beta1.Abci
import cosmos.tx.v1beta1.ServiceOuterClass
import io.grpc.Metadata
import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import io.grpc.stub.AbstractStub
import io.grpc.stub.MetadataUtils
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.frameworks.provenance.exceptions.ContractTxException
import io.provenance.api.frameworks.provenance.extensions.getBaseAccount
import io.provenance.api.frameworks.provenance.extensions.getErrorResult
import io.provenance.api.frameworks.provenance.extensions.isError
import io.provenance.api.frameworks.provenance.extensions.toTxBody
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.contracts.SmartContractConfig
import io.provenance.client.grpc.BaseReqSigner
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import io.provenance.client.grpc.Signer
import io.provenance.metadata.v1.ScopeRequest
import io.provenance.metadata.v1.ScopeResponse
import io.provenance.scope.contract.proto.Contracts
import io.provenance.scope.sdk.SignedResult
import java.net.URI
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Component
import tech.figure.classification.asset.client.client.base.ACClient
import tech.figure.classification.asset.client.client.base.BroadcastOptions
import tech.figure.classification.asset.client.client.base.ContractIdentifier
import tech.figure.classification.asset.client.domain.execute.OnboardAssetExecute
import tech.figure.classification.asset.client.domain.execute.VerifyAssetExecute
import tech.figure.classification.asset.util.objects.ACObjectMapperUtil

class ProvenanceTxException(message: String) : Exception(message)

sealed class ProvenanceTx
class SingleTx(val value: SignedResult) : ProvenanceTx()
class BatchTx(val value: Collection<SignedResult>) : ProvenanceTx()

object ProvenanceConst {
    const val BLOCK_HEIGHT = "x-cosmos-block-height"
}

@Component
class ProvenanceService : Provenance {
    private val cachedSequenceMap = ConcurrentHashMap<String, CachedAccountSequence>()

    override fun buildContractTx(tx: ProvenanceTx): Iterable<Any> =
        when (tx) {
            is SingleTx -> {
                when (val error = tx.getErrorResult()) {
                    null -> {
                        tx.value.messages.map { Any.pack(it, "") }
                    }
                    else -> throw ContractTxException(error.result.errorMessage)
                }
            }
            is BatchTx -> {
                when (val error = tx.getErrorResult()) {
                    emptyList<Contracts.ConsiderationProto?>() -> {
                        tx.value.flatMap { it.messages.map { Any.pack(it, "") } }
                    }
                    else -> throw ContractTxException("Tx Batch operation failed: $error")
                }
            }
        }

    override fun executeTransaction(config: ProvenanceConfig, messages: Iterable<Any>, signer: Signer): Abci.TxResponse =
        tryAction(config, signer) { pbClient, account, offset ->
            val baseSigner = BaseReqSigner(
                signer,
                account = account,
                sequenceOffset = offset,
            )

            val tx = config.timeoutHeight?.let { messages.toTxBody(timeoutHeight = it) } ?: messages.toTxBody(pbClient)

            val result = pbClient.estimateAndBroadcastTx(
                txBody = tx,
                signers = listOf(baseSigner),
                gasAdjustment = config.gasAdjustment,
                mode = config.broadcastMode,
                feeGranter = config.feeGranter
            )

            if (result.isError()) {
                throw ProvenanceTxException(result.txResponse.toString())
            }
            result
        }.txResponse

    @Suppress("DEPRECATION")
    override fun getScope(config: ProvenanceConfig, scopeUuid: UUID, height: Long?): ScopeResponse =
        PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).use { pbClient ->
            pbClient.metadataClient.scope(
                ScopeRequest.newBuilder().apply {
                    scopeId = scopeUuid.toString()
                    includeRecords = true
                    includeSessions = true
                    includeRequest = true
                    height?.let { pbClient.metadataClient.addBlockHeight(it.toString()) }
                }.build()
            )
        }

    override fun classifyAsset(config: ProvenanceConfig, signer: Signer, contractConfig: SmartContractConfig, onboardAssetRequest: OnboardAssetExecute<UUID>): TxResponse =
        tryAction(config, signer) { pbClient, account, offset ->
            val assetClassificationClient = ACClient.getDefault(
                contractIdentifier = ContractIdentifier.Name(contractConfig.contractName),
                pbClient = pbClient,
                objectMapper = ACObjectMapperUtil.getObjectMapper()
            )

            assetClassificationClient.onboardAsset(
                onboardAssetRequest, signer,
                options = BroadcastOptions(
                    broadcastMode = config.broadcastMode,
                    sequenceOffset = offset,
                    baseAccount = account,
                    timeoutHeight = config.timeoutHeight,
                )
            )
        }.txResponse.toTxResponse()

    override fun verifyAsset(config: ProvenanceConfig, signer: Signer, contractConfig: SmartContractConfig, verifyAssetRequest: VerifyAssetExecute<UUID>): TxResponse =
        tryAction(config, signer) { pbClient, account, offset ->

            val assetClassificationClient = ACClient.getDefault(
                contractIdentifier = ContractIdentifier.Name(contractConfig.contractName),
                pbClient = pbClient,
                objectMapper = ACObjectMapperUtil.getObjectMapper()
            )

            assetClassificationClient.verifyAsset(
                verifyAssetRequest, signer,
                options = BroadcastOptions(
                    broadcastMode = config.broadcastMode,
                    sequenceOffset = offset,
                    baseAccount = account,
                    timeoutHeight = config.timeoutHeight,
                )
            )
        }.txResponse.toTxResponse()

    private fun tryAction(config: ProvenanceConfig, signer: Signer, action: (pbClient: PbClient, account: Auth.BaseAccount, offset: Int) -> ServiceOuterClass.BroadcastTxResponse): ServiceOuterClass.BroadcastTxResponse {
        PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).use { pbClient ->
            synchronized(this) {
                val account = getBaseAccount(pbClient, signer.address())
                val cachedOffset = cachedSequenceMap.getOrPut(signer.address()) { CachedAccountSequence() }

                return runCatching {
                    action(pbClient, account, cachedOffset.getAndIncrementOffset(account.sequence))
                }.getOrElse {
                    cachedOffset.getAndDecrement(account.sequence)
                    throw it
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
fun <S : AbstractStub<S>> S.addBlockHeight(blockHeight: String): S = this.also {
    val metadata = Metadata().also {
        it.put(
            Metadata.Key.of(ProvenanceConst.BLOCK_HEIGHT, ASCII_STRING_MARSHALLER),
            blockHeight,
        )
    }

    MetadataUtils.attachHeaders(this, metadata)
}
