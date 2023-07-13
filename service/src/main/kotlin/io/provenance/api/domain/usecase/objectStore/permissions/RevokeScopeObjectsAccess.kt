package io.provenance.api.domain.usecase.objectStore.permissions

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.objectStore.permissions.model.RevokeScopeObjectsAccessRequestWrapper
import io.provenance.api.domain.usecase.objectStore.store.CreateGatewayJwt
import io.provenance.api.domain.usecase.objectStore.store.models.CreateGatewayJwtRequest
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.api.frameworks.provenance.ProvenanceService
import java.net.URI
import org.springframework.stereotype.Component
import tech.figure.objectstore.gateway.client.ClientConfig
import tech.figure.objectstore.gateway.client.GatewayClient

@Component
class RevokeScopeObjectsAccess(
    private val createGatewayJwt: CreateGatewayJwt,
    private val provenanceProperties: ProvenanceProperties,
    private val provenanceService: ProvenanceService
) : AbstractUseCase<RevokeScopeObjectsAccessRequestWrapper, Unit>() {
    override suspend fun execute(args: RevokeScopeObjectsAccessRequestWrapper) {
        val jwt = createGatewayJwt.execute(
            CreateGatewayJwtRequest(
                args.userID,
                args.request.accountInfo.keyManagementConfig
            )
        )

        val scope = provenanceService.getScope(args.request.provenanceConfig, args.request.scopeUuid)
        GatewayClient(ClientConfig(URI(args.request.gatewayUri), provenanceProperties.mainnet)).use { client ->
            scope.recordsList.forEach {
                val hash = it.record.outputsList.first().hash
                client.revokeObjectPermissions(
                    hash,
                    args.request.grantee,
                    jwt
                )
            }
        }
    }
}
