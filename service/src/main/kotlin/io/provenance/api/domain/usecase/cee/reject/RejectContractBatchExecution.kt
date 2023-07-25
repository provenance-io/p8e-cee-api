package io.provenance.api.domain.usecase.cee.reject

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.common.client.CreateClient
import io.provenance.api.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.api.domain.usecase.cee.reject.models.RejectContractBatchRequestWrapper
import io.provenance.scope.contract.proto.Envelopes
import org.springframework.stereotype.Component

@Component
class RejectContractBatchExecution(
    private val createClient: CreateClient
) : AbstractUseCase<RejectContractBatchRequestWrapper, Unit>() {
    override suspend fun execute(args: RejectContractBatchRequestWrapper) {
        createClient.execute(CreateClientRequest(args.entityID, args.request.account, args.request.client)).use { client ->
            args.request.rejection.forEach {
                val error = Envelopes.EnvelopeError.newBuilder().mergeFrom(it).build()
                client.respondWithError(error)
            }
        }
    }
}
