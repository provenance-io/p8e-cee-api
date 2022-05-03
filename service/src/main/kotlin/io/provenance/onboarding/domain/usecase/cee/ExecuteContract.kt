package io.provenance.onboarding.domain.usecase.cee

import com.google.protobuf.Message
import io.provenance.core.KeyType
import io.provenance.onboarding.domain.cee.ContractParser
import io.provenance.onboarding.domain.cee.ContractService
import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.cee.model.ExecuteContractRequest
import io.provenance.onboarding.domain.usecase.common.model.TxResponse
import io.provenance.onboarding.domain.usecase.common.originator.GetOriginator
import io.provenance.onboarding.domain.usecase.provenance.account.GetAccount
import io.provenance.onboarding.frameworks.provenance.utility.ProvenanceUtils
import io.provenance.scope.contract.annotations.Input
import io.provenance.scope.contract.spec.P8eContract
import io.provenance.scope.encryption.model.DirectKeyRef
import io.provenance.scope.encryption.util.toJavaPrivateKey
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.sdk.Affiliate
import io.provenance.scope.sdk.Client
import io.provenance.scope.sdk.ClientConfig
import io.provenance.scope.sdk.SharedClient
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.net.URI
import java.security.KeyPair
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.functions

private val log = KotlinLogging.logger { }

@Component
class ExecuteContract(
    private val getOriginator: GetOriginator,
    private val contractService: ContractService,
    private val provenanceService: Provenance,
    private val getAccount: GetAccount,
    private val contractParser: ContractParser,
) : AbstractUseCase<ExecuteContractRequest, TxResponse>() {

    override suspend fun execute(args: ExecuteContractRequest): TxResponse {
        val utils = ProvenanceUtils()
        val account = getAccount.execute(args.config.account)
        val originator = getOriginator.execute(args.config.account.originatorUuid)
        val affiliate = Affiliate(
            signingKeyRef = DirectKeyRef(KeyPair(originator.keys[KeyType.SIGNING_PUBLIC_KEY].toString().toJavaPublicKey(), originator.keys[KeyType.SIGNING_PRIVATE_KEY].toString().toJavaPrivateKey())),
            encryptionKeyRef = DirectKeyRef(KeyPair(originator.keys[KeyType.SIGNING_PUBLIC_KEY].toString().toJavaPublicKey(), originator.keys[KeyType.SIGNING_PRIVATE_KEY].toString().toJavaPrivateKey())),
            args.config.account.partyType,
        )

        val sharedClient = SharedClient(
            ClientConfig(
                mainNet = !args.config.account.isTestNet,
                cacheJarSizeInBytes = 4L * 1024 * 1024, // ~ 4 MB,
                cacheRecordSizeInBytes = 0L,
                cacheSpecSizeInBytes = 0L,
                disableContractLogs = !args.config.account.isTestNet,
                osConcurrencySize = 6,
                osGrpcUrl = URI(args.config.client.objectStoreUrl),
                osChannelCustomizeFn = { channelBuilder ->
                    channelBuilder
                        .idleTimeout(1, TimeUnit.MINUTES)
                        .keepAliveTime(10, TimeUnit.SECONDS)
                        .keepAliveTimeout(10, TimeUnit.SECONDS)
                }
            )
        )

        val contract = contractService.getContract(args.config.contract.contractName)
        val records = getRecords(args.records, contract)

        val client = Client(sharedClient, affiliate)
        val session = contractService.setupContract(client, contract, records, args.config.contract.scopeUuid, args.config.contract.sessionUuid)
        val signer = utils.getSigner(account)

        contractService.executeContract(client, session) { tx ->
            provenanceService.executeTransaction(args.config.provenanceConfig, session, tx, signer)
        }.fold(
            onSuccess = { result ->
                log.info("[L: ${session.scopeUuid}, S: ${session.sessionUuid}] ${contract.simpleName} is pending. The tx hash is ${result.txhash}.")
                return TxResponse(result.txhash, result.gasWanted.toString(), result.gasUsed.toString(), result.height.toString())
            },
            onFailure = { throwable ->
                log.error("[L: ${session.scopeUuid}, S: ${session.sessionUuid}] ${contract.simpleName} has failed execution. An error occurred.", throwable)
                throw throwable
            }
        )
    }

    @Suppress("TooGenericExceptionCaught")
    private fun getRecords(records: Map<String, Any>, contract: Class<out P8eContract>): Map<String, Message> {
        val contractRecords = mutableMapOf<String, Message>()

        try {
            contract.kotlin.functions.forEach { func ->
                func.parameters.forEach { param ->
                    (param.annotations.firstOrNull { it is Input } as? Input)?.let { input ->
                        val parameterClass = Class.forName(param.type.toString())
                        val recordToParse = records.getOrDefault(input.name, null)
                            ?: throw IllegalStateException("Contract required input record with name ${input.name} but none was found!")
                        val record = contractParser.parseInput(recordToParse, parameterClass)
                        contractRecords[input.name] = record
                    }
                }
            }
        } catch (ex: Exception) {
            log.error("Failed to get inputs for contract ${contract.simpleName}")
            throw ex
        }

        return contractRecords
    }
}
