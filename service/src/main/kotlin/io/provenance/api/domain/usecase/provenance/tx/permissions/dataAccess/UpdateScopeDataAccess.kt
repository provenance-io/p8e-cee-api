package io.provenance.api.domain.usecase.provenance.tx.permissions.dataAccess

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.objectStore.store.CreateGatewayJwt
import io.provenance.api.domain.usecase.objectStore.store.models.CreateGatewayJwtRequest
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.tx.permissions.dataAccess.models.UpdateScopeDataAccessRequestWrapper
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.frameworks.provenance.extensions.isError
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.tx.permissions.dataAccess.DataAccessChangeType
import io.provenance.api.models.p8e.tx.permissions.dataAccess.DataAccessUpdate
import io.provenance.api.util.toPrettyJson
import io.provenance.client.protobuf.extensions.toAny
import io.provenance.metadata.v1.MsgAddScopeDataAccessRequest
import io.provenance.metadata.v1.MsgDeleteScopeDataAccessRequest
import io.provenance.scope.util.MetadataAddress
import io.provenance.scope.util.toByteString
import java.net.URI
import org.springframework.stereotype.Component
import tech.figure.objectstore.gateway.client.ClientConfig
import tech.figure.objectstore.gateway.client.GatewayClient

@Component
class UpdateScopeDataAccess(
    private val provenanceService: ProvenanceService,
    private val getSigner: GetSigner,
    private val provenanceProperties: ProvenanceProperties,
    private val createGatewayJwt: CreateGatewayJwt,
) : AbstractUseCase<UpdateScopeDataAccessRequestWrapper, TxResponse>() {
    override suspend fun execute(args: UpdateScopeDataAccessRequestWrapper): TxResponse {
        val signer = getSigner.execute(GetSignerRequest(args.entityID, args.request.account))

        val messages = runActionForChange(
            args.request.changes,
            {
                MsgAddScopeDataAccessRequest.newBuilder()
                    .setScopeId(MetadataAddress.forScope(args.request.scopeUuid).bytes.toByteString())
                    .addDataAccess(it.address)
                    .addAllSigners(listOf(signer.address()))
                    .build().toAny()
            },
            {
                MsgDeleteScopeDataAccessRequest.newBuilder()
                    .setScopeId(MetadataAddress.forScope(args.request.scopeUuid).bytes.toByteString())
                    .addDataAccess(it.address)
                    .addAllSigners(listOf(signer.address()))
                    .build().toAny()
            }
        )

        return provenanceService.executeTransaction(args.request.provenanceConfig, messages, signer)
            .takeIf { !it.isError() }?.let {
                args.request.objectStoreConfig?.let { osConfig ->
                    GatewayClient(
                        ClientConfig(
                            URI.create(osConfig.objectStoreUrl),
                            provenanceProperties.mainnet
                        )
                    )
                }?.use { client ->
                    val jwt = createGatewayJwt.execute(CreateGatewayJwtRequest(args.entityID, args.request.account.keyManagementConfig))
                    runActionForChange(
                        args.request.changes, { change ->
                        client.grantScopePermission(
                            MetadataAddress.forScope(args.request.scopeUuid).toString(),
                            change.address,
                            jwt
                        )
                    },
                        { change ->
                            client.revokeScopePermission(
                                MetadataAddress.forScope(args.request.scopeUuid).toString(),
                                change.address,
                                jwt
                            )
                        }
                    )
                }

                it.toTxResponse()
            } ?: throw IllegalStateException("Failed to transact against provenance when updating scope permissions!")
    }

    private inline fun <reified T> runActionForChange(changes: List<DataAccessUpdate>, addAction: (change: DataAccessUpdate) -> T, removeAction: (change: DataAccessUpdate) -> T): List<T> {
        val errors = mutableListOf<Throwable>()

        val result = changes.map { change ->
            when (change.type) {
                DataAccessChangeType.ADD -> {
                    runCatching {
                        addAction(change)
                    }
                        .fold(
                            onSuccess = { result -> result },
                            onFailure = { error -> errors.add(error) }
                        )
                }
                DataAccessChangeType.REMOVE -> {
                    runCatching {
                        removeAction(change)
                    }
                        .fold(
                            onSuccess = { result -> result },
                            onFailure = { error -> errors.add(error) }
                        )
                }
            }
        }

        if (errors.any()) {
            throw IllegalStateException("Failed to run action for scope change: ${errors.toPrettyJson()}")
        }

        return result.filterIsInstance<T>()
    }
}
