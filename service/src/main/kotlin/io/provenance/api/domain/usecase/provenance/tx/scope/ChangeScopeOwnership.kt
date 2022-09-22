package io.provenance.api.domain.usecase.provenance.tx.scope

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.errors.NotFoundError
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.tx.scope.models.ChangeScopeOwnershipRequestWrapper
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.api.frameworks.provenance.extensions.toAny
import io.provenance.api.frameworks.provenance.extensions.toBase64String
import io.provenance.api.frameworks.provenance.extensions.toMessageSet
import io.provenance.api.frameworks.provenance.extensions.toTxBody
import io.provenance.api.models.p8e.TxBody
import io.provenance.api.models.p8e.TxResponse
import io.provenance.client.protobuf.extensions.isSet
import io.provenance.metadata.v1.MsgWriteScopeRequest
import io.provenance.metadata.v1.Party
import io.provenance.metadata.v1.PartyType
import io.provenance.scope.util.ProtoJsonUtil.toJson
import org.springframework.stereotype.Component

@Component
class ChangeScopeOwnership(
    private val provenance: Provenance,
    private val entityManager: EntityManager,
    private val getSigner: GetSigner,
    private val provenanceProperties: ProvenanceProperties,
) : AbstractUseCase<ChangeScopeOwnershipRequestWrapper, TxResponse>() {
    override suspend fun execute(args: ChangeScopeOwnershipRequestWrapper): TxResponse {
        require(args.request.newValueOwner !== null || args.request.newDataAccess !== null) {
            "Must request at least one change to the scope"
        }

        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.account))

        val scopeResponse = provenance.getScope(args.request.provenanceConfig, args.request.scopeId)
            .takeIf { response -> response.scope.isSet() }
            ?: throw NotFoundError("No scope found")

        scopeResponse.scope.scopeIdInfo.scopeUuid.let { actualScopeId ->
            require(actualScopeId === args.request.scopeId.toString()) {
                "Expected to fetch a scope with UUID ${args.request.scopeId} but got a UUID of $actualScopeId"
            }
        }

        val message = MsgWriteScopeRequest.newBuilder().apply {
            scopeUuid = args.request.scopeId.toString()
            specUuid = scopeResponse.scope.scopeSpecIdInfo.scopeSpecUuid
            scope = scopeResponse.scope.scope.toBuilder().also { scopeBuilder ->
                /** Only change owner if [valueOwnerAddress][args.request.valueOwnerAddress] was not null. */
                args.request.newValueOwner?.let { requestedNewValueOwner ->
                    scopeBuilder.valueOwnerAddress = requestedNewValueOwner
                    scopeBuilder.ownersList.filter { owner ->
                        owner.role == PartyType.PARTY_TYPE_OWNER
                    }.forEach { existingOwner ->
                        scopeBuilder.removeOwners(scopeBuilder.ownersList.indexOf(existingOwner))
                    }
                    scopeBuilder.addOwners(
                        Party.newBuilder().also { ownerPartyBuilder ->
                            ownerPartyBuilder.role = PartyType.PARTY_TYPE_OWNER
                            ownerPartyBuilder.address = requestedNewValueOwner
                        }.build()
                    )
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

        return provenance.onboard(
            args.request.provenanceConfig.chainId,
            args.request.provenanceConfig.nodeEndpoint,
            signer,
            listOf(message.toAny()).toTxBody().let { cosmosTxBody ->
                TxBody(
                    json = ObjectMapper().readValue(cosmosTxBody.toJson(), ObjectNode::class.java),
                    base64 = cosmosTxBody.messagesList.map { cosmosTxBody.toByteArray().toBase64String() },
                )
            },
        )
    }
}
