package io.provenance.api.domain.usecase.cee.execute

import io.provenance.api.domain.cee.ContractParser
import io.provenance.api.domain.cee.ContractService
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.ContractUtilities
import io.provenance.api.domain.usecase.cee.common.client.CreateClient
import io.provenance.api.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.api.domain.usecase.cee.execute.model.ExecuteContractBatchRequestWrapper
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.provenance.BatchTx
import io.provenance.api.frameworks.provenance.SingleTx
import io.provenance.api.models.cee.execute.ContractExecutionResponse
import io.provenance.api.models.p8e.TxResponse
import io.provenance.client.protobuf.extensions.isSet
import io.provenance.metadata.v1.ScopeResponse
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.sdk.ExecutionResult
import io.provenance.scope.sdk.FragmentResult
import io.provenance.scope.sdk.SignedResult
import java.util.Base64

class ExecuteContractBatch(

    private val contractService: ContractService,
    private val provenanceService: Provenance,
    private val getSigner: GetSigner,
    private val contractParser: ContractParser,
    private val createClient: CreateClient,
    private val entityManager: EntityManager,
): AbstractUseCase<ExecuteContractBatchRequestWrapper, Unit>() {
    override suspend fun execute(args: ExecuteContractBatchRequestWrapper) {
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.config.account))
        val audiences = entityManager.hydrateKeys(args.request.permissions, args.request.participants)
        val client = createClient.execute(CreateClientRequest(args.uuid, args.request.config.account, args.request.config.client, audiences))
        val contract = contractService.getContract(args.request.config.contract.contractName)

        val participants = args.request.participants.associate {
            it.partyType to entityManager.getEntity(KeyManagementConfigWrapper(it.uuid, args.request.config.account.keyManagementConfig))
        }

        val results = mutableListOf<ExecutionResult>()
            args.request.batch.forEach {

                val records = ContractUtilities.getRecords(contractParser, it.records, contract, args.request.config.contract.parserConfig)
                val scope = provenanceService.getScope(args.request.config.provenanceConfig, it.scopeUuid)
                val scopeToUse: ScopeResponse? = if (scope.scope.scope.isSet() && !scope.scope.scope.scopeId.isEmpty) scope else null
                val session = contractService.setupContract(
                    client,
                    contract,
                    records,
                    it.scopeUuid,
                    it.sessionUuid,
                    participants,
                    scopeToUse,
                    args.request.config.contract.scopeSpecificationName,
                    audiences.map { it.encryptionKey.toJavaPublicKey() }.toSet()
                )

                results.add(contractService.executeContract(client, session))
            }


        val responses = mutableListOf<ContractExecutionResponse>()

            results.filterIsInstance(SignedResult::class.java).chunked(args.request.chunkSize).forEach {
                provenanceService.buildContractTx(args.request.config.provenanceConfig, BatchTx(it))?.let { tx ->
                    provenanceService.executeTransaction(args.request.config.provenanceConfig, tx, signer)
                } ?: throw IllegalStateException("Failed to build contract for execution output.")
            }
            }

            results.chunked(args.request.chunkSize).forEach {
                when (it.all { it is SignedResult }) {
                    true -> {
                        provenanceService.buildContractTx(args.request.config.provenanceConfig, SingleTx(result))?.let {
                            provenanceService.executeTransaction(args.request.config.provenanceConfig, it, signer).let { pbResponse ->

                            }
                        }
                    }
                    is FragmentResult -> {
                        client.requestAffiliateExecution(result.envelopeState)
                        ContractExecutionResponse(true, Base64.getEncoder().encodeToString(result.envelopeState.toByteArray()), null)
                    }
                    else -> throw IllegalStateException("Contract execution result was not of an expected type.")
                }
            }

        }
    }
}
