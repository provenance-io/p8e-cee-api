package io.provenance.api.domain.usecase.cee.submit

import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.submit.models.SubmitContractBatchExecutionResultRequestWrapper
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.provenance.BatchTx
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.cee.submit.SubmitContractBatchErrorResponse
import io.provenance.api.models.cee.submit.SubmitContractBatchExecutionResponseWrapper
import io.provenance.api.models.cee.submit.SubmitContractBatchExecutionResultResponse
import io.provenance.api.util.toPrettyJson
import io.provenance.scope.contract.proto.Envelopes
import io.provenance.scope.sdk.SignedResult
import io.provenance.scope.sdk.extensions.mergeInto
import io.provenance.scope.util.scopeOrNull
import java.util.UUID
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class SubmitContractBatchExecutionResult(
    private val provenanceService: Provenance,
    private val getSigner: GetSigner,
) : AbstractUseCase<SubmitContractBatchExecutionResultRequestWrapper, SubmitContractBatchExecutionResponseWrapper>() {

    private val log = KotlinLogging.logger { }

    override suspend fun execute(args: SubmitContractBatchExecutionResultRequestWrapper): SubmitContractBatchExecutionResponseWrapper {
        val signedResults = mutableListOf<Pair<UUID, SignedResult>>()
        val response = mutableListOf<SubmitContractBatchExecutionResultResponse>()
        val errors = mutableListOf<SubmitContractBatchErrorResponse>()
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.account))

        args.request.submission.forEach {
            val envelope = Envelopes.Envelope.newBuilder().mergeFrom(it.envelope).build()
            val state = Envelopes.EnvelopeState.newBuilder().mergeFrom(it.state).build()
            val scopeUuid = UUID.fromString(envelope.scopeOrNull()?.scope?.scopeIdInfo?.scopeUuid)
            when (val result = envelope.mergeInto(state)) {
                is SignedResult -> {
                    signedResults.add(Pair(scopeUuid, result))
                }
                else -> errors.add(
                    SubmitContractBatchErrorResponse(
                        "Merge Failure",
                        "Received a execution result which was not a signed result.",
                        listOf(scopeUuid)
                    )
                )
            }
        }

        val chunked = signedResults.chunked(args.request.chunkSize)
        chunked.forEachIndexed { index, pair ->
            val results = pair.map { it.second }
            runCatching {
                provenanceService.buildContractTx(BatchTx(results)).let { tx ->
                    provenanceService.executeTransaction(args.request.provenance, tx, signer).let { pbResponse ->
                        response.add(
                            SubmitContractBatchExecutionResultResponse(
                                tx = pbResponse.toTxResponse(),
                                scopeUuids = pair.map { it.first }
                            )
                        )
                    }
                }
            }.fold(
                onSuccess = {
                    log.info("Successfully processed batch $index of ${pair.size}")
                },
                onFailure = {
                    errors.add(
                        SubmitContractBatchErrorResponse(
                            "Tx Execution Exception",
                            it.toPrettyJson(),
                            pair.map { it.first }
                        )
                    )
                }
            )
        }

        return SubmitContractBatchExecutionResponseWrapper(
            response,
            errors
        )
    }
}
