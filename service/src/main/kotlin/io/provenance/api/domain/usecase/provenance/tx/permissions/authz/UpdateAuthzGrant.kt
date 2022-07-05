package io.provenance.api.domain.usecase.provenance.tx.permissions.authz

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.common.client.CreateClient
import io.provenance.api.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.tx.permissions.authz.models.UpdateAuthzRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.tx.permissions.authz.AuthzChangeType
import io.provenance.client.protobuf.extensions.toAny
import io.provenance.client.protobuf.extensions.toTxBody
import io.provenance.scope.contract.proto.Envelopes
import java.util.Base64
import org.springframework.stereotype.Component

@Component
class UpdateAuthzGrant(
    private val createClient: CreateClient,
    private val provenanceService: ProvenanceService,
    private val getSigner: GetSigner
) : AbstractUseCase<UpdateAuthzRequestWrapper, TxResponse>() {
    override suspend fun execute(args: UpdateAuthzRequestWrapper): TxResponse {
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.account))
        val messages = createClient.execute(CreateClientRequest(args.uuid, args.request.account, args.request.client)).use { client ->
            args.request.changes.flatMap { change ->
                when (change.type) {
                    AuthzChangeType.ADD -> {
                        client.approveScopeUpdate(Envelopes.EnvelopeState.newBuilder().mergeFrom(Base64.getDecoder().decode(change.envelopeState)).build(), change.expiration).map { it.toAny() }
                    }
                    AuthzChangeType.REMOVE -> {
                        client.revokeScopeUpdate(Envelopes.EnvelopeState.newBuilder().mergeFrom(Base64.getDecoder().decode(change.envelopeState)).build()).map { it.toAny() }
                    }
                }
            }.toTxBody()
        }

        return provenanceService.executeTransaction(args.request.provenanceConfig, messages, signer).toTxResponse()
    }
}
