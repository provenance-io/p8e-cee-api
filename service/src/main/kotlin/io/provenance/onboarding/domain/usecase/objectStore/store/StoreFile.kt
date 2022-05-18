package io.provenance.onboarding.domain.usecase.objectStore.store

import com.google.gson.Gson
import io.provenance.api.models.eos.StoreAssetResponse
import io.provenance.api.models.p8e.AudienceKeyPair
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.onboarding.domain.objectStore.ObjectStore
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.common.originator.EntityManager
import io.provenance.onboarding.domain.usecase.objectStore.store.models.StoreFileRequestWrapper
import io.provenance.onboarding.frameworks.config.ObjectStoreConfig
import io.provenance.onboarding.util.awaitAllBytes
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import java.net.URI
import java.security.PublicKey
import java.util.UUID
import mu.KotlinLogging
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.FormFieldPart
import org.springframework.http.codec.multipart.Part
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.AssetOuterClassBuilders
import tech.figure.proto.util.FileNFT
import tech.figure.proto.util.toProtoAny

private val log = KotlinLogging.logger { }

@Component
class StoreFile(
    private val objectStore: ObjectStore,
    private val objectStoreConfig: ObjectStoreConfig,
    private val entityManager: EntityManager,
): AbstractUseCase<StoreFileRequestWrapper, StoreAssetResponse>() {
    override suspend fun execute(args: StoreFileRequestWrapper): StoreAssetResponse {
        val originator = entityManager.getEntity(args.uuid)
        var additionalAudiences = emptySet<AudienceKeyPair>()
        val osClient = OsClient(URI.create(args.request.getAsType<FormFieldPart>("objectStoreAddress").value()), objectStoreConfig.timeoutMs)
        val file = args.request.getAsType<FilePart>("file")

        args.request["permissions"]?.let {
            val permissions = Gson().fromJson((it as FormFieldPart).value(), PermissionInfo::class.java)
            additionalAudiences = entityManager.hydrateKeys(permissions)
        }

        val asset = AssetOuterClassBuilders.Asset {
            idBuilder.value = UUID.randomUUID().toString()
            type = FileNFT.ASSET_TYPE
            description = file.name()

            putKv(FileNFT.KEY_FILENAME, file.filename().toProtoAny())
            putKv(FileNFT.KEY_BYTES, file.awaitAllBytes().toProtoAny())
            putKv(FileNFT.KEY_SIZE, file.awaitAllBytes().size.toString().toProtoAny())
            putKv(FileNFT.KEY_CONTENT_TYPE, file.headers().contentType.toString().toProtoAny())
        }

        return objectStore.storeAsset(osClient, asset, originator.encryptionPublicKey() as PublicKey, additionalAudiences.map { it.encryptionKey.toJavaPublicKey() }.toSet())
    }

    private inline fun <reified T> Map<String, Part>.getAsType(key: String): T =
        T::class.java.cast(get(key))
            ?: throw IllegalArgumentException("Failed to retrieve and cast provided argument.")

}