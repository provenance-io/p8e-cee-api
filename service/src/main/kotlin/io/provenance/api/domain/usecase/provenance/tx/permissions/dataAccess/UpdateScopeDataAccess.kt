package io.provenance.api.domain.usecase.provenance.tx.permissions.dataAccess

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.tx.permissions.dataAccess.models.UpdateScopeDataAccessRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.tx.permissions.dataAccess.DataAccessChangeType
import io.provenance.client.protobuf.extensions.toAny
import io.provenance.client.protobuf.extensions.toTxBody
import io.provenance.metadata.v1.MsgAddScopeDataAccessRequest
import io.provenance.metadata.v1.MsgDeleteScopeDataAccessRequest
import io.provenance.scope.util.MetadataAddress
import io.provenance.scope.util.toByteString
import org.springframework.stereotype.Component

@Component
class UpdateScopeDataAccess(
    private val provenanceService: ProvenanceService,
    private val getSigner: GetSigner,
) : AbstractUseCase<UpdateScopeDataAccessRequestWrapper, TxResponse>() {
    override suspend fun execute(args: UpdateScopeDataAccessRequestWrapper): TxResponse {
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.account))

        val messages = args.request.changes.map {
            when (it.type) {
                DataAccessChangeType.ADD -> {
                    MsgAddScopeDataAccessRequest.newBuilder()
                        .setScopeId(MetadataAddress.forScope(args.request.scopeUuid).bytes.toByteString())
                        .addDataAccess(it.address)
                        .addAllSigners(listOf(signer.address()))
                        .build().toAny()
                }
                DataAccessChangeType.REMOVE -> {
                    MsgDeleteScopeDataAccessRequest.newBuilder()
                        .setScopeId(MetadataAddress.forScope(args.request.scopeUuid).bytes.toByteString())
                        .addDataAccess(it.address)
                        .addAllSigners(listOf(signer.address()))
                        .build().toAny()
                }
            }
        }.toTxBody()

        return provenanceService.executeTransaction(args.request.provenanceConfig, messages, signer).toTxResponse().also {
        }
    }
}
