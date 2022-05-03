package io.provenance.onboarding.domain.usecase.objectStore

import io.dartinc.registry.v1beta1.ENote
import io.provenance.core.KeyType
import io.provenance.onboarding.domain.objectStore.ObjectStore
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.common.originator.GetOriginator
import io.provenance.onboarding.domain.usecase.objectStore.model.GetAssetRequest
import io.provenance.onboarding.frameworks.config.ObjectStoreConfig
import io.provenance.onboarding.frameworks.provenance.extensions.decodeBase64ToString
import io.provenance.onboarding.util.toPrettyJson
import io.provenance.scope.encryption.util.toJavaPrivateKey
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import io.provenance.scope.objectstore.util.base64Decode
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.Asset
import java.lang.IllegalStateException
import java.net.URI

@Component
class GetAsset(
    private val objectStore: ObjectStore,
    private val getOriginator: GetOriginator,
    private val objectStoreConfig: ObjectStoreConfig,
) : AbstractUseCase<GetAssetRequest, String>() {
    override suspend fun execute(args: GetAssetRequest): String {
        val originator = getOriginator.execute(args.originatorUuid)
        val osClient = OsClient(URI.create(args.objectStoreAddress), objectStoreConfig.timeoutMs)
        val publicKey = (originator.keys[KeyType.ENCRYPTION_PUBLIC_KEY] as? String)?.toJavaPublicKey()
            ?: throw IllegalStateException("Public key was not present for originator: ${args.originatorUuid}")

        val privateKey = (originator.keys[KeyType.ENCRYPTION_PRIVATE_KEY] as? String)?.toJavaPrivateKey()
            ?: throw IllegalStateException("Private key was not present for originator: ${args.originatorUuid}")

        val asset = objectStore.retrieveAndDecrypt(osClient, args.hash.base64Decode(), publicKey, privateKey)
        return asset.decodeToString()
    }
}
