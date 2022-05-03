package io.provenance.onboarding.domain.usecase.cee.submit

import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.cee.submit.model.SubmitContractExecutionResultRequest
import io.provenance.onboarding.domain.usecase.common.model.TxResponse
import io.provenance.onboarding.domain.usecase.provenance.account.GetAccount
import io.provenance.onboarding.frameworks.provenance.SingleTx
import io.provenance.onboarding.frameworks.provenance.utility.ProvenanceUtils
import io.provenance.scope.contract.proto.Envelopes
import io.provenance.scope.sdk.SignedResult
import io.provenance.scope.sdk.extensions.mergeInto
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

@Component
class SubmitContractExecutionResult(
    private val provenanceService: Provenance,
    private val getAccount: GetAccount,
): AbstractUseCase<SubmitContractExecutionResultRequest, Unit>() {
    override suspend fun execute(args: SubmitContractExecutionResultRequest) {

        val utils = ProvenanceUtils()
        val account = getAccount.execute(args.account)
        val signer = utils.getSigner(account)

        val envelope = Envelopes.Envelope.newBuilder()
        val state = Envelopes.EnvelopeState.newBuilder()

        try {
            envelope.mergeFrom(args.envelope)
            state.mergeFrom(args.state)
        } catch (ex: Exception) {
            log.error("Failed to parse the envelope / envelope state input.", ex)
        }

        when (val result = envelope.build().mergeInto(state.build())) {
            is SignedResult -> {
                provenanceService.buildContractTx(args.provenance, SingleTx(result))?.let {
                    provenanceService.executeTransaction(args.provenance, it, signer).let { pbResponse ->
                        TxResponse(pbResponse.txhash, pbResponse.gasWanted.toString(), pbResponse.gasUsed.toString(), pbResponse.height.toString())
                    }
                } ?: throw IllegalStateException("Failed to build the contract tx with the supplied signed result.")
            }
            else -> throw IllegalStateException("Received a execution result which was not a signed result.")
        }
    }
}
