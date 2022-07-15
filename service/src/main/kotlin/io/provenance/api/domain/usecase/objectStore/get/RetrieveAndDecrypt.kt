package io.provenance.api.domain.usecase.objectStore.get

import io.provenance.api.domain.objectStore.ObjectStore
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.objectStore.get.models.RetrieveAndDecryptRequest
import io.provenance.api.frameworks.config.ObjectStoreConfig
import io.provenance.scope.objectstore.client.OsClient
import java.net.URI
import java.security.PrivateKey
import java.security.PublicKey
import org.apache.commons.codec.binary.Base64
import org.springframework.stereotype.Component

@Component
class RetrieveAndDecrypt(
    private val objectStore: ObjectStore,
    private val entityManager: EntityManager,
    private val objectStoreConfig: ObjectStoreConfig,
) : AbstractUseCase<RetrieveAndDecryptRequest, ByteArray>() {
    override suspend fun execute(args: RetrieveAndDecryptRequest): ByteArray {
        val originator = entityManager.getEntity(KeyManagementConfigWrapper(args.uuid.toString(), args.keyManagementConfig))
        val publicKey = (originator.encryptionPublicKey() as? PublicKey)
            ?: throw IllegalStateException("Public key was not present for originator: ${args.uuid}")

        val privateKey = (originator.encryptionPrivateKey() as? PrivateKey)
            ?: throw IllegalStateException("Private key was not present for originator: ${args.uuid}")

        OsClient(URI.create(args.objectStoreAddress), objectStoreConfig.timeoutMs).use { osClient ->
            return objectStore.retrieveAndDecrypt(osClient, decodeBase64(args.hash), publicKey, privateKey)
        }
    }

    private fun decodeBase64(string: String): ByteArray =
        string.replace(' ', '+').let {
            Base64.decodeBase64(it)
        }
}
