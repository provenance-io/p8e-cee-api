package io.provenance.api.domain.usecase.provenance.tx.scope

import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.errors.NotFoundError
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.tx.scope.models.ChangeScopeOwnershipBatchRequestWrapper
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.api.frameworks.provenance.extensions.toAny
import io.provenance.api.frameworks.provenance.extensions.toMessageSet
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.p8e.TxResponse
import io.provenance.client.protobuf.extensions.isSet
import io.provenance.metadata.v1.MsgWriteScopeRequest
import io.provenance.metadata.v1.Party
import io.provenance.metadata.v1.PartyType
import org.springframework.stereotype.Component

@Component
class ChangeScopeOwnership(
    private val provenance: Provenance,
    private val entityManager: EntityManager,
    private val getSigner: GetSigner,
    private val provenanceProperties: ProvenanceProperties,
) : AbstractUseCase<ChangeScopeOwnershipBatchRequestWrapper, TxResponse>() {
    override suspend fun execute(args: ChangeScopeOwnershipBatchRequestWrapper): TxResponse {
        require(args.request.newValueOwner !== null || args.request.newDataAccess !== null) {
            "Must request at least one change to the scope"
        }

        val signer = getSigner.execute(GetSignerRequest(args.Entity, args.request.account))

        val messages = args.request.scopeIds.distinct().map {
            val scopeResponse = provenance.getScope(args.request.provenanceConfig, it)
                .takeIf { response -> response.scope.isSet() }
                ?: throw NotFoundError("No scope found")

            MsgWriteScopeRequest.newBuilder().apply {
                scopeUuid = it.toString()
                specUuid = scopeResponse.scope.scopeSpecIdInfo.scopeSpecUuid
                scope = scopeResponse.scope.scope.toBuilder().also { scopeBuilder ->
                    /** Only change owner if [valueOwnerAddress][args.request.valueOwnerAddress] was not null. */
                    args.request.newValueOwner?.let { requestedNewValueOwner ->
                        scopeBuilder.valueOwnerAddress = requestedNewValueOwner
                        scopeBuilder.ownersList.filter { owner ->
                            owner.role == PartyType.PARTY_TYPE_OWNER
                        }.forEach { existingOwner ->
                            scopeBuilder.removeOwners(scopeBuilder.ownersList.indexOf(existingOwner))
                            scopeBuilder.addOwners(
                                Party.newBuilder().also { ownerPartyBuilder ->
                                    ownerPartyBuilder.role = PartyType.PARTY_TYPE_OWNER
                                    ownerPartyBuilder.address = requestedNewValueOwner
                                }.build()
                            )
                        }
                    }

                    /** Only change data access if [newDataAccess][args.request.newDataAccess] was not null. */
                    args.request.newDataAccess?.let { requestedNewDataAccess ->
                        entityManager.hydrateKeys(requestedNewDataAccess).toMessageSet(
                            isMainnet = provenanceProperties.mainnet
                        ).let { additionalAudiences ->
                            scopeBuilder.clearDataAccess()
                            scopeBuilder.addAllDataAccess(additionalAudiences)
                        }
                    }
                }.build()
                addSigners(signer.address())
            }.build()
        }

        return provenance.executeTransaction(
            args.request.provenanceConfig,
            messages.toAny(),
            signer,
        ).toTxResponse()
    }
}
