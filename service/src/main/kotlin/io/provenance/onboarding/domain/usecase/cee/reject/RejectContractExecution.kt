package io.provenance.onboarding.domain.usecase.cee.reject

import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.cee.common.client.CreateClient
import io.provenance.onboarding.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.onboarding.domain.usecase.cee.reject.model.RejectContractRequest
import io.provenance.scope.contract.proto.Envelopes
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class RejectContractExecution(
    private val createClient: CreateClient
) : AbstractUseCase<RejectContractRequest, Unit>() {

    private val log = KotlinLogging.logger { }

    override suspend fun execute(args: RejectContractRequest) {
        val client = createClient.execute(CreateClientRequest(args.account, args.client))
        val error = Envelopes.EnvelopeError.newBuilder()

        try {
            error.mergeFrom(args.rejection)
        } catch (ex: Exception) {
            log.error("Failed to parse envelope error!", ex)
        }

        client.respondWithError(error.build())
    }
}
