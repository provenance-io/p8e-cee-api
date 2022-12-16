package io.provenance.api.domain.usecase.objectStore.store

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.objectStore.store.models.StoreObjectRequest
import io.provenance.api.domain.usecase.objectStore.store.models.StoreProtoRequestWrapper
import io.provenance.api.frameworks.cee.parsers.MessageParser
import io.provenance.api.models.eos.store.StoreProtoResponse
import java.security.PrivateKey
import java.security.PublicKey
import org.springframework.stereotype.Component

@Component
class StoreProto(
    private val parser: MessageParser,
    private val storeObject: StoreObject,
    private val entityManager: EntityManager,
) : AbstractUseCase<StoreProtoRequestWrapper, StoreProtoResponse>() {
    override suspend fun execute(args: StoreProtoRequestWrapper): StoreProtoResponse {
        val asset = parser.parse(args.request.message, Class.forName(args.request.type))
        val originator = entityManager.getEntity(KeyManagementConfigWrapper(args.uuid.toString(), args.request.account.keyManagementConfig))
        val publicKey = (originator.encryptionPublicKey() as? PublicKey)
            ?: throw IllegalStateException("Public key was not present for originator: ${args.uuid}")

        val privateKey = (originator.encryptionPrivateKey() as? PrivateKey)
            ?: throw IllegalStateException("Private key was not present for originator: ${args.uuid}")

        return storeObject.execute(
            StoreObjectRequest(
                asset.toByteArray(),
                args.request.type,
                args.request.objectStoreAddress,
                args.useObjectStoreGateway,
                publicKey,
                privateKey,
                args.request.permissions,
                args.request.account,
            )
        )
    }
}
