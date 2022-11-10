package io.provenance.api.frameworks.provenance

import com.google.gson.Gson
import com.google.protobuf.Any
import cosmos.auth.v1beta1.Auth
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
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.TxBody
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.contracts.SmartContractConfig
import io.provenance.api.models.p8e.contracts.VOSmartContractConfig
import io.provenance.api.models.p8e.contracts.VOSmartContractLibraryClientCall
import io.provenance.api.util.toPrettyJson
import io.provenance.client.grpc.BaseReqSigner
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import io.provenance.client.grpc.Signer
import io.provenance.metadata.v1.ScopeRequest
import io.provenance.metadata.v1.ScopeResponse
import io.provenance.scope.contract.proto.Contracts
import io.provenance.scope.sdk.SignedResult
import org.springframework.stereotype.Component
import tech.figure.classification.asset.client.client.base.ACClient
import tech.figure.classification.asset.client.client.base.BroadcastOptions
import tech.figure.classification.asset.client.client.base.ContractIdentifier
import tech.figure.classification.asset.client.domain.execute.OnboardAssetExecute
import tech.figure.classification.asset.client.domain.execute.VerifyAssetExecute
import tech.figure.classification.asset.util.objects.ACObjectMapperUtil
import tech.figure.validationoracle.client.client.base.VOClient
import tech.figure.validationoracle.client.client.impl.DefaultVOQuerier
import tech.figure.validationoracle.util.objects.VOObjectMapperUtil
import java.net.URI
import java.util.Base64
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KCallable

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

    override fun executeTransaction(
        config: ProvenanceConfig,
        tx: TxOuterClass.TxBody,
        signer: Signer,
    ): Abci.TxResponse =
        tryAction(config, signer) { pbClient, account, offset ->
            val baseSigner = BaseReqSigner(
                signer,
                account = account,
                sequenceOffset = offset
            )

            val result = pbClient.estimateAndBroadcastTx(
                txBody = tx,
                signers = listOf(baseSigner),
                gasAdjustment = config.gasAdjustment,
                mode = config.broadcastMode
            )

            if (result.isError()) {
                throw ProvenanceTxException(result.txResponse.toString())
            }
            result
        }.txResponse

    override fun onboard(chainId: String, nodeEndpoint: String, signer: Signer, storeTxBody: TxBody): TxResponse =
        tryAction(ProvenanceConfig(chainId, nodeEndpoint), signer) { pbClient, _, _ ->
            val txBody = TxOuterClass.TxBody.newBuilder().also { txBodyBuilder ->
                txBodyBuilder.addAllMessages(
                    storeTxBody.base64.map { tx ->
                        Any.parseFrom(Base64.getDecoder().decode(tx))
                    }
                )
            }.build()

            pbClient.estimateAndBroadcastTx(
                txBody = txBody,
                signers = listOf(BaseReqSigner(signer)),
                mode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_SYNC,
                gasAdjustment = 1.5
            )
        }.txResponse.toTxResponse()

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

    override fun classifyAsset(
        config: ProvenanceConfig,
        signer: Signer,
        contractConfig: SmartContractConfig,
        onboardAssetRequest: OnboardAssetExecute<UUID>,
    ): TxResponse =
        tryAction(config, signer) { pbClient, account, offset ->
            val assetClassificationClient = ACClient.getDefault(
                contractIdentifier = ContractIdentifier.Name(contractConfig.contractName),
                pbClient = pbClient,
                objectMapper = ACObjectMapperUtil.getObjectMapper()
            )

            assetClassificationClient.onboardAsset(
                onboardAssetRequest, signer,
                options = BroadcastOptions(
                    broadcastMode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_BLOCK,
                    sequenceOffset = offset,
                    baseAccount = account
                )
            )
        }.txResponse.toTxResponse()

    override fun executeValidationOracleTransaction(
        config: ProvenanceConfig,
        signer: Signer,
        contractConfig: VOSmartContractConfig,
        libraryCall: VOSmartContractLibraryClientCall
    ): TxResponse =
        tryAction(config, signer) { pbClient, account, offset ->

            val clazz = Class.forName(libraryCall.parameterClassName)
            val executeClass = Gson().fromJson(libraryCall.parameterClassJson, clazz)

            val validationClient = VOClient.getDefault(
                contractIdentifier = tech.figure.validationoracle.client.client.base.ContractIdentifier.Address(
                    contractConfig.contractAddress),
                pbClient = pbClient,
                objectMapper = VOObjectMapperUtil.getObjectMapper()
            )
            val methods: Collection<KCallable<*>> = validationClient.javaClass.kotlin.members
            // @TODO Add type checking here
            @Suppress("UNCHECKED_CAST") val method: KCallable<ServiceOuterClass.BroadcastTxResponse> =
                methods.find { it.name == libraryCall.methodName } as KCallable<ServiceOuterClass.BroadcastTxResponse>

            method.call(
                validationClient,
                executeClass,
                signer,
                tech.figure.validationoracle.client.client.base.BroadcastOptions(
                    broadcastMode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_BLOCK,
                    sequenceOffset = offset,
                    baseAccount = account
                )
            )

        }.txResponse.toTxResponse()

    override fun queryValidationOracle(
        config: ProvenanceConfig,
        contractConfig: VOSmartContractConfig,
        libraryCall: VOSmartContractLibraryClientCall
    ): String {
        val clazz = Class.forName(libraryCall.parameterClassName)
        val queryClass = Gson().fromJson(libraryCall.parameterClassJson, clazz)

        val pbClient = PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION)
        val voQueryClient = DefaultVOQuerier(
            contractIdentifier = tech.figure.validationoracle.client.client.base.ContractIdentifier.Address(
                contractConfig.contractAddress),
            pbClient = pbClient,
            objectMapper = ACObjectMapperUtil.getObjectMapper()
        )

        val methods: Collection<KCallable<*>> = voQueryClient.javaClass.kotlin.members
        // @TODO Add type checking here
        @Suppress("UNCHECKED_CAST") val method: KCallable<ServiceOuterClass.BroadcastTxResponse> =
            methods.find { it.name == libraryCall.methodName } as KCallable<ServiceOuterClass.BroadcastTxResponse>

        return method.call(
            voQueryClient,
            queryClass
        ).toPrettyJson()
    }

    override fun verifyAsset(
        config: ProvenanceConfig,
        signer: Signer,
        contractConfig: SmartContractConfig,
        verifyAssetRequest: VerifyAssetExecute<UUID>,
    ): TxResponse =
        tryAction(config, signer) { pbClient, account, offset ->

            val assetClassificationClient = ACClient.getDefault(
                contractIdentifier = ContractIdentifier.Name(contractConfig.contractName),
                pbClient = pbClient,
                objectMapper = ACObjectMapperUtil.getObjectMapper()
            )

            assetClassificationClient.verifyAsset(
                verifyAssetRequest, signer,
                options = BroadcastOptions(
                    broadcastMode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_BLOCK,
                    sequenceOffset = offset,
                    baseAccount = account
                )
            )
        }.txResponse.toTxResponse()

    fun tryAction(
        config: ProvenanceConfig,
        signer: Signer,
        action: (pbClient: PbClient, account: Auth.BaseAccount, offset: Int) -> ServiceOuterClass.BroadcastTxResponse,
    ): ServiceOuterClass.BroadcastTxResponse {
        PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).use { pbClient ->
            val account = getBaseAccount(pbClient, signer.address())
            val cachedOffset = cachedSequenceMap.getOrPut(signer.address()) { CachedAccountSequence() }

            runCatching {
                action(pbClient, account, cachedOffset.getAndIncrementOffset(account.sequence))
            }.fold(
                onSuccess = {
                    return it
                },
                onFailure = {
                    cachedOffset.getAndDecrement(account.sequence)
                    throw it
                }
            )
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
