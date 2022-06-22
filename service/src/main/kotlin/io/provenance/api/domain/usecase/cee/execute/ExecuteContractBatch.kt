package io.provenance.api.domain.usecase.cee.execute

import io.provenance.api.domain.cee.ContractService
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.common.ContractUtilities
import io.provenance.api.domain.usecase.cee.execute.model.ExecuteContractBatchRequestWrapper
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.provenance.BatchTx
import io.provenance.api.frameworks.provenance.exceptions.ContractExecutionBatchException
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.cee.execute.ContractExecutionResponse
import io.provenance.scope.sdk.ExecutionResult
import io.provenance.scope.sdk.FragmentResult
import io.provenance.scope.sdk.SignedResult
import java.util.Base64
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class ExecuteContractBatch(
    private val contractService: ContractService,
    private val provenanceService: Provenance,
    private val getSigner: GetSigner,
    private val contractUtilities: ContractUtilities,
) : AbstractUseCase<ExecuteContractBatchRequestWrapper, List<ContractExecutionResponse>>() {

    private val log = KotlinLogging.logger { }

    override suspend fun execute(args: ExecuteContractBatchRequestWrapper): List<ContractExecutionResponse> {
        val responses = mutableListOf<ContractExecutionResponse>()
        val errors = mutableListOf<Throwable>()
        val results = mutableListOf<ExecutionResult>()
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.config.account))
        contractUtilities.createClient(args.uuid, args.request.permissions, args.request.participants, args.request.config).use { client ->

            contractUtilities.createSession(
                args.uuid,
                args.request.permissions,
                args.request.participants,
                args.request.config,
                args.request.records,
                args.request.scopes,
            ).forEach {
                results.add(contractService.executeContract(client, it))
            }

            val chunkedSignedResult = results.filterIsInstance(SignedResult::class.java).chunked(args.request.chunkSize)
            chunkedSignedResult.forEachIndexed { index, it ->
                runCatching {
                    provenanceService.buildContractTx(args.request.config.provenanceConfig, BatchTx(it)).let { tx ->
                        provenanceService.executeTransaction(args.request.config.provenanceConfig, tx, signer).let { pbResponse ->
                            responses.add(
                                ContractExecutionResponse(
                                    false,
                                    null,
                                    pbResponse.toTxResponse()
                                )
                            )
                        }
                    }
                }.fold(
                    onSuccess = {
                        log.info("Successfully processed batch $index of ${chunkedSignedResult.size}")
                    },
                    onFailure = {
                        errors.add(it)
                    }
                )
            }

            val chunkedFragResult = results.filterIsInstance(FragmentResult::class.java).chunked(args.request.chunkSize)
            chunkedFragResult.forEachIndexed { index, chunk ->
                runCatching {
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
                }.fold(
                    onSuccess = {
                        log.info("Successfully processed batch $index of ${chunkedFragResult.size}")
                    },
                    onFailure = {
                        errors.add(it)
                    }
                )
            }

            if (errors.any()) {
                throw ContractExecutionBatchException(errors.joinToString(limit = 20) { it.message.toString() })
            }

            return responses
        }
    }
}
