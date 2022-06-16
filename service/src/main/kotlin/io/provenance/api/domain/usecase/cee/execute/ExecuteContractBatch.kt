package io.provenance.api.domain.usecase.cee.execute

import io.provenance.api.domain.cee.ContractService
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.ContractUtilities
import io.provenance.api.domain.usecase.cee.execute.model.ExecuteContractBatchRequestWrapper
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.provenance.BatchTx
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.cee.execute.ContractExecutionResponse
import io.provenance.scope.sdk.ExecutionResult
import io.provenance.scope.sdk.FragmentResult
import io.provenance.scope.sdk.SignedResult
import java.util.Base64
import org.springframework.stereotype.Component

@Component
class ExecuteContractBatch(
    private val contractService: ContractService,
    private val provenanceService: Provenance,
    private val getSigner: GetSigner,
    private val contractUtilities: ContractUtilities,
) : AbstractUseCase<ExecuteContractBatchRequestWrapper, List<ContractExecutionResponse>>() {
    override suspend fun execute(args: ExecuteContractBatchRequestWrapper): List<ContractExecutionResponse> {
        val responses = mutableListOf<ContractExecutionResponse>()
        val results = mutableListOf<ExecutionResult>()
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.config.account))
        val client = contractUtilities.createClient(args.uuid, args.request.permissions, args.request.participants, args.request.config)

        contractUtilities.createSession(args.uuid, args.request.permissions, args.request.participants, args.request.config, args.request.records, args.request.scopes).forEach {
            results.add(contractService.executeContract(client, it))
        }

        results.filterIsInstance(SignedResult::class.java).chunked(args.request.chunkSize).forEach {
            provenanceService.buildContractTx(args.request.config.provenanceConfig, BatchTx(it))?.let { tx ->
                provenanceService.executeTransaction(args.request.config.provenanceConfig, tx, signer).let { pbResponse ->
                    responses.add(
                        ContractExecutionResponse(
                            false,
                            null,
                            pbResponse.toTxResponse()
                        )
                    )
                }
            } ?: throw IllegalStateException("Failed to build contract for execution output.")
        }

        results.filterIsInstance(FragmentResult::class.java).chunked(args.request.chunkSize).forEach { chunk ->
            chunk.forEach { result ->
                client.requestAffiliateExecution(result.envelopeState)
                responses.add(
                    ContractExecutionResponse(
                        true,
                        Base64.getEncoder().encodeToString(result.envelopeState.toByteArray()),
                        null
                    )
                )
            }
        }

        return responses
    }
}
