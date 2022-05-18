package io.provenance.onboarding.domain.usecase.objectStore.get

import io.provenance.onboarding.domain.objectStore.ObjectStore
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.common.originator.EntityManager
import io.provenance.onboarding.domain.usecase.objectStore.get.models.GetFileRequestWrapper
import io.provenance.onboarding.frameworks.config.ObjectStoreConfig
import io.provenance.onboarding.util.toPrettyJson
import io.provenance.scope.objectstore.client.OsClient
import io.provenance.scope.objectstore.util.base64Decode
import java.lang.IllegalStateException
import java.net.URI
import java.security.PrivateKey
import java.security.PublicKey
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.Asset

@Component
class GetFile(
    private val objectStore: ObjectStore,
    private val entityManager: EntityManager,
    private val objectStoreConfig: ObjectStoreConfig,
): AbstractUseCase<GetFileRequestWrapper, String>() {
    override suspend fun execute(args: GetFileRequestWrapper): String {
        val originator = entityManager.getEntity(args.uuid)
        val osClient = OsClient(URI.create(args.request.objectStoreAddress), objectStoreConfig.timeoutMs)
        val publicKey = (originator.encryptionPublicKey() as? PublicKey)
            ?: throw IllegalStateException("Public key was not present for originator: ${args.uuid}")

        val privateKey = (originator.encryptionPrivateKey() as? PrivateKey)
            ?: throw IllegalStateException("Private key was not present for originator: ${args.uuid}")

        val asset = objectStore.retrieveAndDecrypt(osClient, args.request.hash.base64Decode(), publicKey, privateKey)
        return Asset.parseFrom(asset).toPrettyJson()
    }
}
