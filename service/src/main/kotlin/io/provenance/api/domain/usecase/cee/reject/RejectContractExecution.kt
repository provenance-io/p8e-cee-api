package io.provenance.api.domain.usecase.cee.reject

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.common.client.CreateClient
import io.provenance.api.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.api.domain.usecase.cee.reject.models.RejectContractExecutionRequestWrapper
import io.provenance.scope.contract.proto.Envelopes
import org.springframework.stereotype.Component

@Component
class RejectContractExecution(
    private val createClient: CreateClient
) : AbstractUseCase<RejectContractExecutionRequestWrapper, Unit>() {

    override suspend fun execute(args: RejectContractExecutionRequestWrapper) {
        val error = Envelopes.EnvelopeError.newBuilder().mergeFrom(args.request.rejection).build()
        createClient.execute(CreateClientRequest(args.userID, args.request.account, args.request.client)).use { client ->
            client.respondWithError(error)
        }
    }
}
