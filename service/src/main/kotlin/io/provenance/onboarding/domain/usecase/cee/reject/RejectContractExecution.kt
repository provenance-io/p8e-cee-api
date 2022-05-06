package io.provenance.onboarding.domain.usecase.cee.reject

import io.provenance.api.models.cee.RejectContractRequest
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.cee.common.client.CreateClient
import io.provenance.onboarding.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.scope.contract.proto.Envelopes
import org.springframework.stereotype.Component

@Component
class RejectContractExecution(
    private val createClient: CreateClient
) : AbstractUseCase<RejectContractRequest, Unit>() {

    override suspend fun execute(args: RejectContractRequest) {
        val client = createClient.execute(CreateClientRequest(args.account, args.client))
        val error = Envelopes.EnvelopeError.newBuilder().mergeFrom(args.rejection).build()
        client.respondWithError(error)
    }
}
