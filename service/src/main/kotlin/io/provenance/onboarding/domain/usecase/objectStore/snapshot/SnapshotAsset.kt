package io.provenance.onboarding.domain.usecase.objectStore.snapshot

import io.provenance.onboarding.domain.objectStore.ObjectStore
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.common.originator.GetOriginator
import io.provenance.onboarding.domain.usecase.objectStore.store.StoreAsset
import io.provenance.onboarding.domain.usecase.objectStore.snapshot.models.SnapshotAssetRequestWrapper
import io.provenance.api.models.eos.StoreAssetRequest
import io.provenance.onboarding.domain.usecase.objectStore.store.models.StoreAssetRequestWrapper
import io.provenance.api.models.eos.StoreAssetResponse
import io.provenance.onboarding.frameworks.config.ObjectStoreConfig
import io.provenance.scope.objectstore.client.OsClient
import io.provenance.scope.objectstore.util.base64Decode
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.Asset
import java.lang.IllegalStateException
import java.net.URI
import java.security.PrivateKey
import java.security.PublicKey
import java.util.UUID

@Component
class SnapshotAsset(
    private val objectStore: ObjectStore,
    private val getOriginator: GetOriginator,
    private val objectStoreConfig: ObjectStoreConfig,
    private val storeAsset: StoreAsset
) : AbstractUseCase<SnapshotAssetRequestWrapper, StoreAssetResponse>() {
    override suspend fun execute(args: SnapshotAssetRequestWrapper): StoreAssetResponse {

        val originator = getOriginator.execute(args.uuid)
        val osClient = OsClient(URI.create(args.request.objectStoreAddress), objectStoreConfig.timeoutMs)
        val publicKey = (originator.signingPublicKey() as? PublicKey)
            ?: throw IllegalStateException("Public key was not present for originator: ${args.uuid}")

        val privateKey = (originator.signingPrivateKey() as? PrivateKey)
            ?: throw IllegalStateException("Private key was not present for originator: ${args.uuid}")

        val asset = objectStore.retrieveAndDecrypt(osClient, args.request.hash.base64Decode(), publicKey, privateKey)
        val snapshot = Asset.parseFrom(asset)

        return storeAsset.execute(
            StoreAssetRequestWrapper(
                args.uuid,
                StoreAssetRequest(
                    args.request.account,
                    args.request.objectStoreAddress,
                    args.request.permissions,
                    UUID.fromString(snapshot.id.value),
                    snapshot.toByteArray().toString()
                )
            )
        )
    }
}
