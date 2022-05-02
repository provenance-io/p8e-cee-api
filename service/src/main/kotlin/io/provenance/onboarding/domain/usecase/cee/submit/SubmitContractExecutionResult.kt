package io.provenance.onboarding.domain.usecase.cee.submit

import com.google.protobuf.util.JsonFormat
import io.provenance.onboarding.domain.cee.ContractService
import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.cee.common.client.CreateClient
import io.provenance.onboarding.domain.usecase.cee.submit.model.SubmitContractExecutionResultRequest
import io.provenance.onboarding.domain.usecase.common.model.TxResponse
import io.provenance.onboarding.domain.usecase.provenance.account.GetAccount
import io.provenance.onboarding.frameworks.provenance.SingleTx
import io.provenance.onboarding.frameworks.provenance.utility.ProvenanceUtils
import io.provenance.onboarding.util.toPrettyJson
import io.provenance.scope.contract.proto.Envelopes
import io.provenance.scope.sdk.FragmentResult
import io.provenance.scope.sdk.SignedResult
import io.provenance.scope.sdk.extensions.mergeInto
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class SubmitContractExecutionResult(
    private val provenanceService: Provenance,
    private val contractService: ContractService,
    private val createClient: CreateClient,
    private val getAccount: GetAccount,
): AbstractUseCase<SubmitContractExecutionResultRequest, Unit>() {

    private val log = KotlinLogging.logger { }

    override suspend fun execute(args: SubmitContractExecutionResultRequest) {

        val utils = ProvenanceUtils()
        val account = getAccount.execute(args.account)
        val signer = utils.getSigner(account)
        val envelope = Envelopes.Envelope.newBuilder()
        val state = Envelopes.EnvelopeState.newBuilder()

        try {
            JsonFormat.parser().merge(args.envelope.toPrettyJson(), envelope)
            JsonFormat.parser().merge(args.state.toPrettyJson(), state)
        } catch (ex: Exception) {
            log.error("You done messed up", ex)
        }

        when (val result = envelope.build().mergeInto(state.build())) {
            is SignedResult -> {
                provenanceService.buildContractTx(args.provenance, SingleTx(result))?.let {
                    provenanceService.executeTransaction(args.provenance, it, signer).let { pbResponse ->
                        TxResponse(pbResponse.txhash, pbResponse.gasWanted.toString(), pbResponse.gasUsed.toString(), pbResponse.height.toString())
                    }
                } ?: throw IllegalStateException("Failed")
            }
            is FragmentResult -> {
                throw IllegalStateException("Should not of gotten a fragment result ya ding dong.")
            }
            else -> throw IllegalStateException("failed")
        }
    }
}
