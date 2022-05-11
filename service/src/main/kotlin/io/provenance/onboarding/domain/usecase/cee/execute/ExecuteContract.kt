package io.provenance.onboarding.domain.usecase.cee.execute

import com.google.protobuf.Message
import io.provenance.api.models.cee.ContractExecutionResponse
import io.provenance.api.models.cee.ExecuteContractRequest
import io.provenance.api.models.p8e.TxResponse
import io.provenance.client.protobuf.extensions.isSet
import io.provenance.metadata.v1.ScopeResponse
import io.provenance.onboarding.domain.cee.ContractParser
import io.provenance.onboarding.domain.cee.ContractService
import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.cee.common.client.CreateClient
import io.provenance.onboarding.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.onboarding.domain.usecase.common.originator.GetOriginator
import io.provenance.onboarding.domain.usecase.provenance.account.GetSigner
import io.provenance.onboarding.frameworks.provenance.SingleTx
import io.provenance.scope.contract.annotations.Input
import io.provenance.scope.contract.spec.P8eContract
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
    private val getOriginator: GetOriginator,
) : AbstractUseCase<ExecuteContractRequest, Any>() {

    override suspend fun execute(args: ExecuteContractRequest): ContractExecutionResponse {
        val signer = getSigner.execute(args.config.account)
        val client = createClient.execute(CreateClientRequest(args.config.account, args.config.client, args.participants))
        val contract = contractService.getContract(args.config.contract.contractName)
        val records = getRecords(args.records, contract)

        val participants = args.participants.associate {
            it.partyType to getOriginator.execute(it.originatorUuid)
        }

        val scope = provenanceService.getScope(args.config.provenanceConfig, args.config.contract.scopeUuid)
        val scopeToUse: ScopeResponse? = if (scope.scope.scope.isSet() && !scope.scope.scope.scopeId.isEmpty) scope else null
        val session = contractService.setupContract(client, contract, records, args.config.contract.scopeUuid, args.config.contract.sessionUuid, participants, scopeToUse, args.config.contract.scopeSpecificationName)

        return when (val result = contractService.executeContract(client, session)) {
            is SignedResult -> {
                provenanceService.buildContractTx(args.config.provenanceConfig, SingleTx(result))?.let {
                    provenanceService.executeTransaction(args.config.provenanceConfig, it, signer).let { pbResponse ->
                        ContractExecutionResponse(false, null, TxResponse(pbResponse.txhash, pbResponse.gasWanted.toString(), pbResponse.gasUsed.toString(), pbResponse.height.toString()))
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
    private fun getRecords(records: Map<String, Any>, contract: Class<out P8eContract>): Map<String, Message> {
        val contractRecords = mutableMapOf<String, Message>()

        try {
            contract.kotlin.functions.forEach { func ->
                func.parameters.forEach { param ->
                    (param.annotations.firstOrNull { it is Input } as? Input)?.let { input ->
                        val parameterClass = Class.forName(param.type.toClassNameString())
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

    private fun KType?.toClassNameString(): String? = this?.classifier?.toString()?.drop("class ".length)
}
