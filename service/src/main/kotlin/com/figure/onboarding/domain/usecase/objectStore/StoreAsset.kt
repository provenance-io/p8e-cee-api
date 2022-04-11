package com.figure.onboarding.domain.usecase.objectStore

import com.figure.onboarding.domain.provenance.ObjectStore
import com.figure.onboarding.domain.usecase.AbstractUseCase
import com.figure.onboarding.domain.usecase.common.originator.GetOriginator
import com.figure.onboarding.domain.usecase.objectStore.model.StoreAssetRequest
import com.figure.onboarding.domain.usecase.objectStore.model.StoreAssetResponse
import com.figure.onboarding.frameworks.config.ObjectStoreConfig
import com.figure.onboarding.frameworks.objectStore.AudienceKeyManager
import com.figure.onboarding.frameworks.objectStore.DefaultAudience
import io.provenance.core.KeyType
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import io.provenance.scope.objectstore.util.base64Decode
import java.lang.IllegalStateException
import java.net.URI
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.AssetOuterClassBuilders
import tech.figure.proto.util.FileNFT
import tech.figure.proto.util.toProtoAny

@Component
class StoreAsset(
    private val objectStore: ObjectStore,
    private val objectStoreConfig: ObjectStoreConfig,
    private val audienceKeyManager: AudienceKeyManager,
    private val getOriginator: GetOriginator,
) : AbstractUseCase<StoreAssetRequest, StoreAssetResponse>() {
    override suspend fun execute(args: StoreAssetRequest): StoreAssetResponse {
        val originator = getOriginator.execute(args.account.originatorUuid)
        val osClient = OsClient(URI.create(args.objectStoreAddress), objectStoreConfig.timeoutMs)
        val additionalAudiences = args.permissions?.audiences?.toMutableSet() ?: mutableSetOf()

        if (args.permissions?.permissionDart == true) {
            additionalAudiences.add(audienceKeyManager.get(DefaultAudience.DART))
        }

        if (args.permissions?.permissionPortfolioManager == true) {
            additionalAudiences.add(audienceKeyManager.get(DefaultAudience.PORTFOLIO_MANAGER))
        }

        val publicKey = (originator.keys[KeyType.ENCRYPTION_PUBLIC_KEY] as? String)?.toJavaPublicKey()
            ?: throw IllegalStateException("Public key was not present for originator: ${args.account.originatorUuid}")

        val asset = AssetOuterClassBuilders.Asset {
            idBuilder.value = args.assetId.toString()
            type = FileNFT.ASSET_TYPE
            putKv(FileNFT.KEY_BYTES, args.asset.base64Decode().toProtoAny())
        }

        return objectStore.storeAsset(osClient, asset, publicKey, additionalAudiences)
    }
}
