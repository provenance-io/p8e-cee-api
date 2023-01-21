package io.provenance.api.domain.usecase.cee.execute

import io.provenance.api.domain.cee.ContractService
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.common.ContractUtilities
import io.provenance.api.domain.usecase.cee.execute.model.ExecuteContractBatchRequestWrapper
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.provenance.BatchTx
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.cee.execute.ContractExecutionBatchResponse
import io.provenance.api.models.cee.execute.ContractExecutionErrorResponse
import io.provenance.api.models.cee.execute.ContractExecutionResponse
import io.provenance.api.models.cee.execute.MultipartyContractExecutionResponse
import io.provenance.api.models.cee.execute.SinglePartyContractExecutionResponse
import io.provenance.api.util.toPrettyJson
import io.provenance.scope.sdk.ExecutionResult
import io.provenance.scope.sdk.FragmentResult
import io.provenance.scope.sdk.SignedResult
import java.util.Base64
import java.util.UUID
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class ExecuteContractBatch(
    private val contractService: ContractService,
    private val provenanceService: Provenance,
    private val getSigner: GetSigner,
    private val contractUtilities: ContractUtilities,
) : AbstractUseCase<ExecuteContractBatchRequestWrapper, ContractExecutionBatchResponse>() {

    private val log = KotlinLogging.logger { }

    override suspend fun execute(args: ExecuteContractBatchRequestWrapper): ContractExecutionBatchResponse {
        val completed = mutableListOf<ContractExecutionResponse>()
        val pending = mutableListOf<ContractExecutionResponse>()
        val errors = mutableListOf<ContractExecutionErrorResponse>()
        val results = mutableListOf<Pair<UUID, ExecutionResult>>()
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.config.account))
        contractUtilities.createClient(args.uuid, args.request.permissions, args.request.additionalParticipants, args.request.config).use { client ->

            contractUtilities.createSession(
                args.uuid,
                client,
                args.request.permissions,
                args.request.additionalParticipants,
                args.request.config,
                args.request.records,
                args.request.scopes,
            ).forEach {
                runCatching {
                    contractService.executeContract(client, it)
                }.fold(
                    onSuccess = { result ->
                        results.add(Pair(it.scopeUuid, result))
                    },
                    onFailure = { error ->
                        errors.add(
                            ContractExecutionErrorResponse(
                                errorType = "Contract Execution Exception",
                                error = error.toPrettyJson(),
                                scopeUuids = listOf(it.scopeUuid)
                            )
                        )
                    }
                )
            }

            val chunkedSignedResult = results.filter { SignedResult::class.java.isInstance(it.second) }.chunked(args.request.chunkSize)
            chunkedSignedResult.forEachIndexed { index, chunk ->
                val executions = chunk.map { it.second as SignedResult }
                runCatching {
                    provenanceService.buildContractTx(BatchTx(executions)).let { tx ->
                        provenanceService.executeTransaction(args.request.config.provenanceConfig, tx, signer).let { pbResponse ->
                            completed.add(
                                SinglePartyContractExecutionResponse(
                                    metadata = pbResponse.toTxResponse(),
                                    scopeUuids = chunk.map { it.first }
                                )
                            )
                        }
                    }
                }.fold(
                    onSuccess = {
                        log.info("Successfully processed batch $index of ${chunkedSignedResult.size}")
                    },
                    onFailure = { error ->
                        errors.add(
                            ContractExecutionErrorResponse(
                                errorType = "Signed Tx Execution Exception",
                                error = error.toPrettyJson(),
                                scopeUuids = chunk.map { it.first }
                            )
                        )
                    }
                )
            }

            val chunkedFragResult = results.filter { FragmentResult::class.java.isInstance(it.second) }.chunked(args.request.chunkSize)
            chunkedFragResult.forEachIndexed { index, chunk ->
                chunk.forEach { result ->
                    runCatching {
                        val fragment = result.second as FragmentResult
                        client.requestAffiliateExecution(fragment.envelopeState)
                        pending.add(
                            MultipartyContractExecutionResponse(
                                envelopeState = Base64.getEncoder().encodeToString(fragment.envelopeState.toByteArray()),
                                scopeUuids = listOf(result.first)
                            )
                        )
                    }.fold(
                        onSuccess = {
                            log.info("Successfully processed batch $index of ${chunkedFragResult.size}")
                        },
                        onFailure = {
                            errors.add(
                                ContractExecutionErrorResponse(
                                    errorType = "Fragment Tx Execution Exception",
                                    error = it.toPrettyJson(),
                                    scopeUuids = listOf(result.first)
                                )
                            )
                        }
                    )
                }
            }

            return ContractExecutionBatchResponse(
                completed,
                pending,
                errors
            )
        }
    }
}
