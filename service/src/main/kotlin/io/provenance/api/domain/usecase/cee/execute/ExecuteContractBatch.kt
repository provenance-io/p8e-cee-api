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
import io.provenance.api.models.cee.execute.ContractExecutionErrorResponse
import io.provenance.api.models.cee.execute.ContractExecutionResponse
import io.provenance.api.models.cee.execute.MultipartyContractExecutionResponse
import io.provenance.api.models.cee.execute.SinglePartyContractExecutionResponse
import io.provenance.api.util.toPrettyJson
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
        val results = mutableListOf<ExecutionResult>()
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.config.account))
        contractUtilities.createClient(args.uuid, args.request.permissions, args.request.participants, args.request.config).use { client ->

            contractUtilities.createSession(
                args.uuid,
                client,
                args.request.permissions,
                args.request.participants,
                args.request.config,
                args.request.records,
                args.request.scopes,
            ).forEach {
                runCatching {
                    contractService.executeContract(client, it)
                }.fold(
                    onSuccess = {
                        results.add(it)
                    },
                    onFailure = {
                        responses.add(
                            ContractExecutionErrorResponse(it.toPrettyJson())
                        )
                    }
                )
            }

            val chunkedSignedResult = results.filterIsInstance(SignedResult::class.java).chunked(args.request.chunkSize)
            chunkedSignedResult.forEachIndexed { index, it ->
                runCatching {
                    provenanceService.buildContractTx(args.request.config.provenanceConfig, BatchTx(it)).let { tx ->
                        provenanceService.executeTransaction(args.request.config.provenanceConfig, tx, signer).let { pbResponse ->
                            responses.add(
                                SinglePartyContractExecutionResponse(
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
                        responses.add(
                            ContractExecutionErrorResponse(it.toPrettyJson())
                        )
                    }
                )
            }

            val chunkedFragResult = results.filterIsInstance(FragmentResult::class.java).chunked(args.request.chunkSize)
            chunkedFragResult.forEachIndexed { index, chunk ->
                runCatching {
                    chunk.forEach { result ->
                        client.requestAffiliateExecution(result.envelopeState)
                        responses.add(
                            MultipartyContractExecutionResponse(
                                Base64.getEncoder().encodeToString(result.envelopeState.toByteArray())
                            )
                        )
                    }
                }.fold(
                    onSuccess = {
                        log.info("Successfully processed batch $index of ${chunkedFragResult.size}")
                    },
                    onFailure = {
                        responses.add(
                            ContractExecutionErrorResponse(it.toPrettyJson())
                        )
                    }
                )
            }

            return responses
        }
    }
}
