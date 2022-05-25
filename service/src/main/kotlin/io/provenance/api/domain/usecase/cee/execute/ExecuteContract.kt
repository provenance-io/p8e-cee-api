package io.provenance.api.domain.usecase.cee.execute

import com.google.protobuf.Message
import io.provenance.api.models.cee.ContractExecutionResponse
import io.provenance.api.models.cee.ParserConfig
import io.provenance.api.models.p8e.TxResponse
import io.provenance.client.protobuf.extensions.isSet
import io.provenance.metadata.v1.ScopeResponse
import io.provenance.api.domain.cee.ContractParser
import io.provenance.api.domain.cee.ContractService
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.common.client.CreateClient
import io.provenance.api.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.api.domain.usecase.cee.execute.model.ExecuteContractRequestWrapper
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.provenance.SingleTx
import io.provenance.scope.contract.annotations.Input
import io.provenance.scope.contract.spec.P8eContract
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.sdk.FragmentResult
import io.provenance.scope.sdk.SignedResult
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.Base64
import kotlin.reflect.KType
import kotlin.reflect.full.functions

private val log = KotlinLogging.logger { }

@Component
class ExecuteContract(
    private val contractService: ContractService,
    private val provenanceService: Provenance,
    private val getSigner: GetSigner,
    private val contractParser: ContractParser,
    private val createClient: CreateClient,
    private val entityManager: EntityManager,
) : AbstractUseCase<ExecuteContractRequestWrapper, ContractExecutionResponse>() {

    override suspend fun execute(args: ExecuteContractRequestWrapper): ContractExecutionResponse {
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.config.account))
        val audiences = entityManager.hydrateKeys(args.request.permissions)
        val client = createClient.execute(CreateClientRequest(args.uuid, args.request.config.account, args.request.config.client, audiences))
        val contract = contractService.getContract(args.request.config.contract.contractName)
        val records = getRecords(args.request.records, contract, args.request.config.contract.parserConfig)

        val participants = args.request.participants.associate {
            it.partyType to entityManager.getEntity(KeyManagementConfigWrapper(it.uuid, args.request.config.account.keyManagementConfig))
        }

        val scope = provenanceService.getScope(args.request.config.provenanceConfig, args.request.config.contract.scopeUuid)
        val scopeToUse: ScopeResponse? = if (scope.scope.scope.isSet() && !scope.scope.scope.scopeId.isEmpty) scope else null
        val session = contractService.setupContract(
            client,
            contract,
            records,
            args.request.config.contract.scopeUuid,
            args.request.config.contract.sessionUuid,
            participants,
            scopeToUse,
            args.request.config.contract.scopeSpecificationName,
            audiences.map { it.encryptionKey.toJavaPublicKey() }.toSet()
        )

        return when (val result = contractService.executeContract(client, session)) {
            is SignedResult -> {
                provenanceService.buildContractTx(args.request.config.provenanceConfig, SingleTx(result))?.let {
                    provenanceService.executeTransaction(args.request.config.provenanceConfig, it, signer).let { pbResponse ->
                        ContractExecutionResponse(
                            false,
                            null,
                            TxResponse(
                                pbResponse.txhash,
                                pbResponse.gasWanted.toString(),
                                pbResponse.gasUsed.toString(),
                                pbResponse.height.toString()
                            )
                        )
                    }
                } ?: throw IllegalStateException("Failed to build contract for execution output.")
            }
            is FragmentResult -> {
                client.requestAffiliateExecution(result.envelopeState)
                ContractExecutionResponse(true, Base64.getEncoder().encodeToString(result.envelopeState.toByteArray()), null)
            }
            else -> throw IllegalStateException("Contract execution result was not of an expected type.")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun getRecords(records: Map<String, Any>, contract: Class<out P8eContract>, parserConfig: ParserConfig?): Map<String, Message> {
        val contractRecords = mutableMapOf<String, Message>()

        try {
            contract.kotlin.functions.forEach { func ->
                func.parameters.forEach { param ->
                    (param.annotations.firstOrNull { it is Input } as? Input)?.let { input ->
                        val parameterClass = Class.forName(param.type.toClassNameString())
                        records.getOrDefault(input.name, null)?.let {

                            val record = when (val parser = parserConfig?.name?.let { name -> contractParser.getParser(name) }) {
                                null -> {
                                    contractParser.parseInput(it, parameterClass)
                                }
                                else -> {
                                    parser.parse(it, parameterClass, parserConfig.descriptors)
                                }
                            }

                            contractRecords[input.name] = record
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            log.error("Failed to get inputs for contract ${contract.simpleName}")
            throw ex
        }

        return contractRecords
    }

    private fun KType?.toClassNameString(): String? = this?.classifier?.toString()?.drop("class ".length)
}
