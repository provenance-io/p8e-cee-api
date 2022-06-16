package io.provenance.api.frameworks.provenance

import com.google.common.io.BaseEncoding
import com.google.protobuf.Any
import cosmos.base.abci.v1beta1.Abci
import cosmos.tx.v1beta1.ServiceOuterClass
import cosmos.tx.v1beta1.TxOuterClass
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
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.TxBody
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.contracts.SmartContractConfig
import io.provenance.classification.asset.client.client.base.ACClient
import io.provenance.classification.asset.client.client.base.BroadcastOptions
import io.provenance.classification.asset.client.client.base.ContractIdentifier
import io.provenance.classification.asset.client.domain.execute.OnboardAssetExecute
import io.provenance.classification.asset.client.domain.execute.VerifyAssetExecute
import io.provenance.classification.asset.util.objects.ACObjectMapperUtil
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
import mu.KotlinLogging
import org.springframework.stereotype.Component

class ProvenanceTxException(message: String) : Exception(message)

sealed class ProvenanceTx
class SingleTx(val value: SignedResult) : ProvenanceTx()
class BatchTx(val value: Collection<SignedResult>) : ProvenanceTx()

object ProvenanceConst {
    const val BLOCK_HEIGHT = "x-cosmos-block-height"
}

@Component
class ProvenanceService : Provenance {

    private val log = KotlinLogging.logger { }
    private val cachedSequenceMap = ConcurrentHashMap<String, CachedAccountSequence>()

    override fun buildContractTx(config: ProvenanceConfig, tx: ProvenanceTx): TxOuterClass.TxBody =
        PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).use { pbClient ->
            return when (tx) {
                is SingleTx -> {
                    when (val error = tx.getErrorResult()) {
                        null -> {
                            val messages = tx.value.messages.map { Any.pack(it, "") }
                            messages.toTxBody(pbClient)
                        }
                        else -> throw ContractTxException(error.result.errorMessage)
                    }
                }
                is BatchTx -> {
                    when (val error = tx.getErrorResult()) {
                        emptyList<Contracts.ConsiderationProto?>() -> {
                            val messages = tx.value.flatMap { it.messages.map { Any.pack(it, "") } }
                            messages.toTxBody(pbClient)
                        }
                        else -> throw ContractTxException("Tx Batch operation failed: $error")
                    }
                }
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
            mode = config.broadcastMode
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

    @Suppress("DEPRECATION")
    override fun getScope(config: ProvenanceConfig, scopeUuid: UUID, height: Long?): ScopeResponse =
        PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).use { pbClient ->
            pbClient.metadataClient.scope(
                ScopeRequest.newBuilder()
                    .setScopeId(scopeUuid.toString())
                    .setIncludeRecords(true)
                    .setIncludeSessions(true)
                    .also {
                        height?.let { pbClient.metadataClient.addBlockHeight(it.toString()) }
                    }
                    .build()
            )
        }

    override fun classifyAsset(config: ProvenanceConfig, signer: Signer, contractConfig: SmartContractConfig, onboardAssetRequest: OnboardAssetExecute<UUID>): TxResponse =
        PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).use {
            val cachedOffset = cachedSequenceMap.getOrPut(signer.address()) { CachedAccountSequence() }
            val account = getBaseAccount(it, signer.address())
            val assetClassificationClient = ACClient.getDefault(
                contractIdentifier = ContractIdentifier.Name(contractConfig.contractName),
                pbClient = it,
                objectMapper = ACObjectMapperUtil.getObjectMapper()
            )

            val broadcast = assetClassificationClient.onboardAsset(
                onboardAssetRequest, signer,
                options = BroadcastOptions(
                    broadcastMode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_BLOCK,
                    sequenceOffset = cachedOffset.getAndIncrementOffset(account.sequence),
                    baseAccount = account
                )
            ).txResponse

            return TxResponse(broadcast.txhash, broadcast.gasWanted.toString(), broadcast.gasUsed.toString(), broadcast.height.toString())
        }

    override fun verifyAsset(config: ProvenanceConfig, signer: Signer, contractConfig: SmartContractConfig, verifyAssetRequest: VerifyAssetExecute<UUID>): TxResponse =
        PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).use {
            val cachedOffset = cachedSequenceMap.getOrPut(signer.address()) { CachedAccountSequence() }
            val account = getBaseAccount(it, signer.address())
            val assetClassificationClient = ACClient.getDefault(
                contractIdentifier = ContractIdentifier.Name(contractConfig.contractName),
                pbClient = it,
                objectMapper = ACObjectMapperUtil.getObjectMapper()
            )

            val broadcast = assetClassificationClient.verifyAsset(
                verifyAssetRequest, signer,
                options = BroadcastOptions(
                    broadcastMode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_BLOCK,
                    sequenceOffset = cachedOffset.getAndIncrementOffset(account.sequence),
                    baseAccount = account
                )
            ).txResponse

            return TxResponse(broadcast.txhash, broadcast.gasWanted.toString(), broadcast.gasUsed.toString(), broadcast.height.toString())
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
