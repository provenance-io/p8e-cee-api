package com.figure.onboarding.domain.usecase.objectStore

import com.figure.onboarding.domain.provenance.ObjectStore
import com.figure.onboarding.domain.usecase.AbstractUseCase
import com.figure.onboarding.domain.usecase.common.originator.GetOriginator
import com.figure.onboarding.domain.usecase.objectStore.model.SnapshotAssetRequest
import com.figure.onboarding.domain.usecase.objectStore.model.StoreAssetRequest
import com.figure.onboarding.domain.usecase.objectStore.model.StoreAssetResponse
import com.figure.onboarding.frameworks.config.ObjectStoreConfig
import io.provenance.core.KeyType
import io.provenance.scope.encryption.util.toJavaPrivateKey
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import io.provenance.scope.objectstore.util.base64Decode
import java.lang.IllegalStateException
import java.net.URI
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.Asset
import java.util.UUID

@Component
class SnapshotAsset(
    private val objectStore: ObjectStore,
    private val getOriginator: GetOriginator,
    private val objectStoreConfig: ObjectStoreConfig,
    private val storeAsset: StoreAsset
) : AbstractUseCase<SnapshotAssetRequest, StoreAssetResponse>() {
    override suspend fun execute(args: SnapshotAssetRequest): StoreAssetResponse {

        val originator = getOriginator.execute(args.account.originatorUuid)
        val osClient = OsClient(URI.create(args.objectStoreAddress), objectStoreConfig.timeoutMs)
        val publicKey = (originator.keys[KeyType.ENCRYPTION_PUBLIC_KEY] as? String)?.toJavaPublicKey()
            ?: throw IllegalStateException("Public key was not present for originator: ${args.account.originatorUuid}")

        val privateKey = (originator.keys[KeyType.SIGNING_PRIVATE_KEY] as? String)?.toJavaPrivateKey()
            ?: throw IllegalStateException("Private key was not present for originator: ${args.account.originatorUuid}")

        val asset = objectStore.retrieveAndDecrypt(osClient, args.hash.base64Decode(), publicKey, privateKey)
        val snapshot = Asset.parseFrom(asset)

        return storeAsset.execute(
            StoreAssetRequest(
                args.account,
                args.objectStoreAddress,
                args.permissions,
                UUID.fromString(snapshot.id.value),
                snapshot.toByteArray().toString()
            )
        )
    }
}
