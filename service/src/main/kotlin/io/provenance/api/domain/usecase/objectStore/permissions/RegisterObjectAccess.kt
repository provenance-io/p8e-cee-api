package io.provenance.api.domain.usecase.objectStore.permissions

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.objectStore.permissions.model.RegisterObjectAccessRequestWrapper
import io.provenance.api.domain.usecase.objectStore.store.CreateGatewayJwt
import io.provenance.api.domain.usecase.objectStore.store.models.CreateGatewayJwtRequest
import io.provenance.api.frameworks.config.ProvenanceProperties
import java.net.URI
import org.springframework.stereotype.Component
import tech.figure.objectstore.gateway.client.ClientConfig
import tech.figure.objectstore.gateway.client.GatewayClient

@Component
class RegisterObjectAccess(
    private val provenanceProperties: ProvenanceProperties,
    private val createGatewayJwt: CreateGatewayJwt,
) : AbstractUseCase<RegisterObjectAccessRequestWrapper, Unit>() {
    override suspend fun execute(args: RegisterObjectAccessRequestWrapper) {

        val jwt = createGatewayJwt.execute(
            CreateGatewayJwtRequest(
                args.Entity,
                args.request.accountInfo.keyManagementConfig
            )
        )
        GatewayClient(ClientConfig(URI(args.request.gatewayUri), provenanceProperties.mainnet)).use { client ->
            client.registerExistingObject(
                args.request.hash,
                args.request.grantee,
                jwt
            )
        }
    }
}
