package io.provenance.onboarding.domain.usecase.objectStore.store

import io.provenance.onboarding.domain.objectStore.ObjectStore
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.common.originator.GetOriginator
import io.provenance.onboarding.domain.usecase.objectStore.store.models.StoreAssetRequestWrapper
import io.provenance.api.models.eos.StoreAssetResponse
import io.provenance.core.KeyType
import io.provenance.onboarding.frameworks.config.ObjectStoreConfig
import io.provenance.onboarding.frameworks.objectStore.AudienceKeyManager
import io.provenance.onboarding.frameworks.objectStore.DefaultAudience
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import io.provenance.scope.objectstore.util.base64Decode
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.AssetOuterClassBuilders
import tech.figure.proto.util.FileNFT
import tech.figure.proto.util.toProtoAny
import java.lang.IllegalStateException
import java.net.URI
import java.security.PublicKey

@Component
class StoreAsset(
    private val objectStore: ObjectStore,
    private val objectStoreConfig: ObjectStoreConfig,
    private val audienceKeyManager: AudienceKeyManager,
    private val getOriginator: GetOriginator,
) : AbstractUseCase<StoreAssetRequestWrapper, StoreAssetResponse>() {
    override suspend fun execute(args: StoreAssetRequestWrapper): StoreAssetResponse {
        val originator = getOriginator.execute(args.uuid)
        val osClient = OsClient(URI.create(args.request.objectStoreAddress), objectStoreConfig.timeoutMs)
        val additionalAudiences: MutableSet<PublicKey> = mutableSetOf()

        args.request.permissions?.audiences?.forEach {
            it.uuid?.let { uuid ->
                val entity = getOriginator.execute(uuid)
                additionalAudiences.add(entity.keys[KeyType.ENCRYPTION_PUBLIC_KEY].toString().toJavaPublicKey())
            } ?: apply {
                it.keys?.let { kp ->
                    additionalAudiences.add(kp.encryptionKey.toJavaPublicKey())
                } ?: throw IllegalStateException("Audience specified does not include entity uuid or key pair.")
            }
        }

        if (args.request.permissions?.permissionDart == true) {
            additionalAudiences.add(audienceKeyManager.get(DefaultAudience.DART))
        }

        if (args.request.permissions?.permissionPortfolioManager == true) {
            additionalAudiences.add(audienceKeyManager.get(DefaultAudience.PORTFOLIO_MANAGER))
        }

        val publicKey = (originator.encryptionPublicKey() as? PublicKey)
            ?: throw IllegalStateException("Public key was not present for originator: ${args.uuid}")

        val asset = AssetOuterClassBuilders.Asset {
            idBuilder.value = args.request.assetId.toString()
            type = FileNFT.ASSET_TYPE
            putKv(FileNFT.KEY_BYTES, args.request.asset.base64Decode().toProtoAny())
        }

        return objectStore.storeAsset(osClient, asset, publicKey, additionalAudiences)
    }
}
