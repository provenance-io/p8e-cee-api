package io.provenance.onboarding.domain.usecase.objectStore.get

import io.provenance.onboarding.domain.objectStore.ObjectStore
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.common.originator.GetOriginator
import io.provenance.onboarding.domain.usecase.objectStore.get.models.GetAssetRequestWrapper
import io.provenance.onboarding.frameworks.config.ObjectStoreConfig
import io.provenance.scope.objectstore.client.OsClient
import io.provenance.scope.objectstore.util.base64Decode
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.net.URI
import java.security.PrivateKey
import java.security.PublicKey

@Component
class GetAsset(
    private val objectStore: ObjectStore,
    private val getOriginator: GetOriginator,
    private val objectStoreConfig: ObjectStoreConfig,
) : AbstractUseCase<GetAssetRequestWrapper, String>() {
    override suspend fun execute(args: GetAssetRequestWrapper): String {
        val originator = getOriginator.execute(args.uuid)
        val osClient = OsClient(URI.create(args.request.objectStoreAddress), objectStoreConfig.timeoutMs)
        val publicKey = (originator.encryptionPublicKey() as? PublicKey)
            ?: throw IllegalStateException("Public key was not present for originator: ${args.uuid}")

        val privateKey = (originator.encryptionPrivateKey() as? PrivateKey)
            ?: throw IllegalStateException("Private key was not present for originator: ${args.uuid}")

        val asset = objectStore.retrieveAndDecrypt(osClient, args.request.hash.base64Decode(), publicKey, privateKey)
        return asset.decodeToString()
    }
}
