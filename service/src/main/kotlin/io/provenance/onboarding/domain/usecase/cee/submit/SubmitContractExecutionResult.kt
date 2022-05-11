package io.provenance.onboarding.domain.usecase.cee.submit

import io.provenance.api.models.p8e.TxResponse
import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.cee.submit.models.SubmitContractExecutionResultRequestWrapper
import io.provenance.onboarding.domain.usecase.provenance.account.GetSigner
import io.provenance.onboarding.frameworks.provenance.SingleTx
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
        val signer = getSigner.execute(args.uuid)

        val envelope = Envelopes.Envelope.newBuilder().mergeFrom(args.request.envelope).build()
        val state = Envelopes.EnvelopeState.newBuilder().mergeFrom(args.request.state).build()

        return when (val result = envelope.mergeInto(state)) {
            is SignedResult -> {
                provenanceService.buildContractTx(args.request.provenance, SingleTx(result))?.let {
                    provenanceService.executeTransaction(args.request.provenance, it, signer).let { pbResponse ->
                        TxResponse(pbResponse.txhash, pbResponse.gasWanted.toString(), pbResponse.gasUsed.toString(), pbResponse.height.toString())
                    }
                } ?: throw IllegalStateException("Failed to build the contract tx with the supplied signed result.")
            }
            else -> throw IllegalStateException("Received a execution result which was not a signed result.")
        }
    }
}
