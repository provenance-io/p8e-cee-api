package io.provenance.api.domain.usecase.objectStore.store

import io.provenance.api.domain.objectStore.ObjectStore
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.objectStore.store.models.StoreProtoRequestWrapper
import io.provenance.api.frameworks.cee.parsers.MessageParser
import io.provenance.api.frameworks.config.ObjectStoreConfig
import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.net.URI
import java.security.PublicKey

@Component
class StoreProto(
    private val objectStore: ObjectStore,
    private val objectStoreConfig: ObjectStoreConfig,
    private val entityManager: EntityManager,
    private val parser: MessageParser
) : AbstractUseCase<StoreProtoRequestWrapper, StoreProtoResponse>() {
    override suspend fun execute(args: StoreProtoRequestWrapper): StoreProtoResponse {
        val originator = entityManager.getEntity(KeyManagementConfigWrapper(args.uuid, args.request.account.keyManagementConfig))
        val osClient = OsClient(URI.create(args.request.objectStoreAddress), objectStoreConfig.timeoutMs)
        val additionalAudiences = entityManager.hydrateKeys(args.request.permissions)

        val asset = parser.parse(args.request.message, Class.forName(args.request.type))

        val publicKey = (originator.encryptionPublicKey() as? PublicKey)
            ?: throw IllegalStateException("Public key was not present for originator: ${args.uuid}")

        return objectStore.storeMessage(osClient, asset, publicKey, additionalAudiences.map { it.encryptionKey.toJavaPublicKey() }.toSet())
    }
}
