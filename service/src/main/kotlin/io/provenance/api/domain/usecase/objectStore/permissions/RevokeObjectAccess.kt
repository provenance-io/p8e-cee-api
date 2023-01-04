package io.provenance.api.domain.usecase.objectStore.permissions

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.objectStore.permissions.model.RevokeObjectAccessRequestWrapper
import io.provenance.api.domain.usecase.objectStore.store.CreateGatewayJwt
import io.provenance.api.domain.usecase.objectStore.store.models.CreateGatewayJwtRequest
import io.provenance.api.frameworks.config.ProvenanceProperties
import java.net.URI
import org.springframework.stereotype.Component
import tech.figure.objectstore.gateway.client.ClientConfig
import tech.figure.objectstore.gateway.client.GatewayClient

@Component
class RevokeObjectAccess(
    private val provenanceProperties: ProvenanceProperties,
    private val createGatewayJwt: CreateGatewayJwt,
) : AbstractUseCase<RevokeObjectAccessRequestWrapper, Unit>() {
    override suspend fun execute(args: RevokeObjectAccessRequestWrapper) {
        val jwt = createGatewayJwt.execute(
            CreateGatewayJwtRequest(
                args.uuid,
                args.request.accountInfo.keyManagementConfig
            )
        )

        GatewayClient(ClientConfig(URI(args.request.gatewayUri), provenanceProperties.mainnet)).use { client ->
            client.revokeObjectPermissions(
                args.request.hash,
                args.request.grantee,
                jwt
            )
        }
    }
}
