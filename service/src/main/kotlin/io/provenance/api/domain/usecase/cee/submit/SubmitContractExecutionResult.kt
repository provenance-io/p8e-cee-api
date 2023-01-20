package io.provenance.api.domain.usecase.cee.submit

import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.submit.models.SubmitContractExecutionResultRequestWrapper
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.provenance.SingleTx
import io.provenance.api.frameworks.provenance.exceptions.ContractExecutionException
import io.provenance.api.models.p8e.TxResponse
import io.provenance.scope.contract.proto.Envelopes
import io.provenance.scope.sdk.SignedResult
import io.provenance.scope.sdk.extensions.mergeInto
import org.springframework.stereotype.Component

@Component
class SubmitContractExecutionResult(
    private val provenanceService: Provenance,
    private val getSigner: GetSigner,
) : AbstractUseCase<SubmitContractExecutionResultRequestWrapper, TxResponse>() {
    override suspend fun execute(args: SubmitContractExecutionResultRequestWrapper): TxResponse {
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.account))

        val envelope = Envelopes.Envelope.newBuilder().mergeFrom(args.request.submission.envelope).build()
        val state = Envelopes.EnvelopeState.newBuilder().mergeFrom(args.request.submission.state).build()

        return when (val result = envelope.mergeInto(state)) {
            is SignedResult -> {
                provenanceService.buildContractTx(SingleTx(result)).let {
                    provenanceService.executeTransaction(args.request.provenance, it, signer).let { pbResponse ->
                        TxResponse(pbResponse.txhash, pbResponse.gasWanted.toString(), pbResponse.gasUsed.toString(), pbResponse.height.toString())
                    }
                }
            }
            else -> throw ContractExecutionException("Received a execution result which was not a signed result.")
        }
    }
}
