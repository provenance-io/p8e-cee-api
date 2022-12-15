package io.provenance.api.domain.usecase.objectStore.store

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.objectStore.store.models.CreateGatewayJwtRequest
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import org.springframework.stereotype.Component
import tech.figure.objectstore.gateway.client.GatewayJwt

@Component
class CreateGatewayJwt(
    private val entityManager: EntityManager
): AbstractUseCase<CreateGatewayJwtRequest, GatewayJwt.KeyPairJwt>() {
    override suspend fun execute(args: CreateGatewayJwtRequest): GatewayJwt.KeyPairJwt {
        val entity = entityManager.getEntity(KeyManagementConfigWrapper(args.uuid.toString(), args.keyManagementConfig))

        val publicKey = (entity.encryptionPublicKey() as? PublicKey)
            ?: throw IllegalStateException("Public key was not present for originator: ${args.uuid}")

        val privateKey = (entity.encryptionPrivateKey() as? PrivateKey)
            ?: throw IllegalStateException("Private key was not present for originator: ${args.uuid}")

        return GatewayJwt.KeyPairJwt(KeyPair(publicKey, privateKey))
    }
}
