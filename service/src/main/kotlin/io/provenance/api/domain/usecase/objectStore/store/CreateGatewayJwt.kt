package io.provenance.api.domain.usecase.objectStore.store

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.objectStore.store.models.CreateGatewayJwtRequest
import io.provenance.entity.KeyType
import org.springframework.stereotype.Component
import tech.figure.objectstore.gateway.client.GatewayJwt

@Component
class CreateGatewayJwt(
    private val entityManager: EntityManager
) : AbstractUseCase<CreateGatewayJwtRequest, GatewayJwt.KeyRefJwt>() {
    override suspend fun execute(args: CreateGatewayJwtRequest): GatewayJwt.KeyRefJwt {
        val entity = entityManager.getEntity(KeyManagementConfigWrapper(args.userID.toString(), args.keyManagementConfig))

        return GatewayJwt.KeyRefJwt(entity.getKeyRef(KeyType.ENCRYPTION))
    }
}
