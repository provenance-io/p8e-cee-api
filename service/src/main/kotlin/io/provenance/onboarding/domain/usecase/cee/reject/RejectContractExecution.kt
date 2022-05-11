package io.provenance.onboarding.domain.usecase.cee.reject

import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.cee.common.client.CreateClient
import io.provenance.onboarding.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.onboarding.domain.usecase.cee.reject.models.RejectContractExecutionRequestWrapper
import io.provenance.scope.contract.proto.Envelopes
import org.springframework.stereotype.Component

@Component
class RejectContractExecution(
    private val createClient: CreateClient
) : AbstractUseCase<RejectContractExecutionRequestWrapper, Unit>() {

    override suspend fun execute(args: RejectContractExecutionRequestWrapper) {
        val client = createClient.execute(CreateClientRequest(args.uuid, args.request.account, args.request.client))
        val error = Envelopes.EnvelopeError.newBuilder().mergeFrom(args.request.rejection).build()
        client.respondWithError(error)
    }
}
