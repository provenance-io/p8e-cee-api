package io.provenance.api.domain.usecase.cee.execute

import io.provenance.api.domain.cee.ContractParser
import io.provenance.api.domain.cee.ContractService
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.ContractUtilities.Companion.getRecords
import io.provenance.api.domain.usecase.cee.common.client.CreateClient
import io.provenance.api.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.api.domain.usecase.cee.execute.model.ExecuteContractRequestWrapper
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.provenance.SingleTx
import io.provenance.api.models.cee.execute.ContractExecutionResponse
import io.provenance.api.models.p8e.TxResponse
import io.provenance.client.protobuf.extensions.isSet
import io.provenance.metadata.v1.ScopeResponse
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.sdk.FragmentResult
import io.provenance.scope.sdk.SignedResult
import java.util.Base64
import org.springframework.stereotype.Component

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
        val audiences = entityManager.hydrateKeys(args.request.permissions, args.request.participants)
        val client = createClient.execute(CreateClientRequest(args.uuid, args.request.config.account, args.request.config.client, audiences))
        val contract = contractService.getContract(args.request.config.contract.contractName)
        val records = getRecords(contractParser, args.request.records, contract, args.request.config.contract.parserConfig)

        val participants = args.request.participants.associate {
            it.partyType to entityManager.getEntity(KeyManagementConfigWrapper(it.uuid, args.request.config.account.keyManagementConfig))
        }

        val scope = provenanceService.getScope(args.request.config.provenanceConfig, args.request.scopeUuid)
        val scopeToUse: ScopeResponse? = if (scope.scope.scope.isSet() && !scope.scope.scope.scopeId.isEmpty) scope else null
        val session = contractService.setupContract(
            client,
            contract,
            records,
            args.request.scopeUuid,
            args.request.sessionUuid,
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
}
