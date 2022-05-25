package io.provenance.api.domain.usecase.objectStore.store

import com.google.gson.Gson
import io.provenance.api.domain.objectStore.ObjectStore
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.models.eos.StoreProtoResponse
import io.provenance.api.models.p8e.AudienceKeyPair
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.frameworks.config.ObjectStoreConfig
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.account.KeyManagementConfig
import io.provenance.api.util.awaitAllBytes
import io.provenance.onboarding.domain.usecase.objectStore.store.models.StoreFileRequestWrapper
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import java.net.URI
import java.security.PublicKey
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.FormFieldPart
import org.springframework.http.codec.multipart.Part
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.AssetOuterClassBuilders
import tech.figure.proto.util.FileNFT
import tech.figure.proto.util.toProtoAny

@Component
class StoreFile(
    private val objectStore: ObjectStore,
    private val objectStoreConfig: ObjectStoreConfig,
    private val entityManager: EntityManager,
) : AbstractUseCase<StoreFileRequestWrapper, StoreProtoResponse>() {
    override suspend fun execute(args: StoreFileRequestWrapper): StoreProtoResponse {

        var keyConfig: KeyManagementConfig? = null
        args.request["account"]?.let {
            keyConfig = Gson().fromJson((it as FormFieldPart).value(), AccountInfo::class.java).keyManagementConfig
        }

        val originator = entityManager.getEntity(KeyManagementConfigWrapper(args.uuid, keyConfig))
        var additionalAudiences = emptySet<AudienceKeyPair>()
        val osClient = OsClient(URI.create(args.request.getAsType<FormFieldPart>("objectStoreAddress").value()), objectStoreConfig.timeoutMs)
        if (!args.request.containsKey("id") || args.request.getAsType<FormFieldPart>("id").value().isEmpty()) {
            throw IllegalArgumentException("Request must provide the 'id' field for the file")
        }
        val file = args.request.getAsType<FilePart>("file")

        args.request["permissions"]?.let {
            val permissions = Gson().fromJson((it as FormFieldPart).value(), PermissionInfo::class.java)
            additionalAudiences = entityManager.hydrateKeys(permissions)
        }

        val asset = AssetOuterClassBuilders.Asset {
            idBuilder.value = args.request.getAsType<FormFieldPart>("id").value()
            type = FileNFT.ASSET_TYPE
            description = file.filename()

            putKv(FileNFT.KEY_FILENAME, file.filename().toProtoAny())
            putKv(FileNFT.KEY_BYTES, file.awaitAllBytes().toProtoAny())
            putKv(FileNFT.KEY_SIZE, file.awaitAllBytes().size.toString().toProtoAny())
            putKv(FileNFT.KEY_CONTENT_TYPE, file.headers().contentType.toString().toProtoAny())
        }

        return objectStore.storeMessage(osClient, asset, originator.encryptionPublicKey() as PublicKey, additionalAudiences.map { it.encryptionKey.toJavaPublicKey() }.toSet())
    }

    private inline fun <reified T> Map<String, Part>.getAsType(key: String): T =
        T::class.java.cast(get(key))
            ?: throw IllegalArgumentException("Failed to retrieve and cast provided argument.")
}
