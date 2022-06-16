package io.provenance.api.domain.usecase.cee.submit

import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.submit.models.SubmitContractBatchExecutionResultRequestWrapper
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.provenance.BatchTx
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.p8e.TxResponse
import io.provenance.scope.contract.proto.Envelopes
import io.provenance.scope.sdk.SignedResult
import io.provenance.scope.sdk.extensions.mergeInto
import org.springframework.stereotype.Component

@Component
class SubmitContractBatchExecutionResult(
    private val provenanceService: Provenance,
    private val getSigner: GetSigner,
) : AbstractUseCase<SubmitContractBatchExecutionResultRequestWrapper, List<TxResponse>>() {
    override suspend fun execute(args: SubmitContractBatchExecutionResultRequestWrapper): List<TxResponse> {
        val signedResults = mutableListOf<SignedResult>()
        val response = mutableListOf<TxResponse>()
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.account))

        args.request.submission.forEach {
        val envelope = Envelopes.Envelope.newBuilder().mergeFrom(it.envelope).build()
        val state = Envelopes.EnvelopeState.newBuilder().mergeFrom(it.state).build()
            when (val result = envelope.mergeInto(state)) {
                is SignedResult -> {
                    signedResults.add(result)
                }
                else -> throw IllegalStateException("Received a execution result which was not a signed result.")
            }
        }

        signedResults.chunked(args.request.chunkSize).forEach { resultCollection ->
            provenanceService.buildContractTx(args.request.provenance, BatchTx(resultCollection))?.let { tx ->
                provenanceService.executeTransaction(args.request.provenance, tx, signer).let { pbResponse ->
                    response.add(pbResponse.toTxResponse())
                }
            } ?: throw IllegalStateException("Failed to build the contract tx with the supplied signed result.")
        }

        return response
    }
}
