package io.provenance.api.domain.usecase.objectStore.store

import com.google.gson.Gson
import io.provenance.api.domain.objectStore.ObjectStore
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.objectStore.store.models.StoreFileRequestWrapper
import io.provenance.api.frameworks.config.ObjectStoreConfig
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.account.KeyManagementConfig
import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.api.models.p8e.AudienceKeyPair
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.api.util.awaitAllBytes
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import java.io.ByteArrayInputStream
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
        val (keyConfig, additionalAudiences, objectStoreAddress, storeRawBytes, id, file) = getParams(args.request)
        val originator = entityManager.getEntity(KeyManagementConfigWrapper(args.uuid.toString(), keyConfig))

        OsClient(URI.create(objectStoreAddress), objectStoreConfig.timeoutMs).use { osClient ->
            val bytes = file.awaitAllBytes()
            ByteArrayInputStream(bytes).use { message ->
                return objectStore.store(
                    osClient,
                    if (!storeRawBytes)
                        AssetOuterClassBuilders.Asset {
                            idBuilder.value = id
                            type = FileNFT.ASSET_TYPE
                            description = file.filename()
                            putKv(FileNFT.KEY_FILENAME, file.filename().toProtoAny())
                            putKv(FileNFT.KEY_BYTES, bytes.toProtoAny())
                            putKv(FileNFT.KEY_SIZE, bytes.size.toString().toProtoAny())
                            putKv(FileNFT.KEY_CONTENT_TYPE, file.headers().contentType.toString().toProtoAny())
                        } else message,
                    originator.encryptionPublicKey() as PublicKey,
                    additionalAudiences.map { it.encryptionKey.toJavaPublicKey() }.toSet()
                )
            }
        }
    }

    private fun getParams(request: Map<String, Part>): Args {
        var additionalAudiences = emptySet<AudienceKeyPair>()
        var keyConfig: KeyManagementConfig? = null

        request["account"]?.let {
            keyConfig = Gson().fromJson((it as FormFieldPart).value(), AccountInfo::class.java).keyManagementConfig
        }

        if (!request.containsKey("id") || request.getAsType<FormFieldPart>("id").value().isEmpty()) {
            throw IllegalArgumentException("Request must provide the 'id' field for the file")
        }

        request["permissions"]?.let {
            val permissions = Gson().fromJson((it as FormFieldPart).value(), PermissionInfo::class.java)
            additionalAudiences = entityManager.hydrateKeys(permissions)
        }

        val objectStoreAddress = request.getAsType<FormFieldPart>("objectStoreAddress").value()
        val storeRawBytes = request.getAsType<FormFieldPart>("storeRawBytes").value().toBoolean()
        val id = request.getAsType<FormFieldPart>("id").value()
        val file = request.getAsType<FilePart>("file")
        return Args(keyConfig, additionalAudiences, objectStoreAddress, storeRawBytes, id, file)
    }

    private inline fun <reified T> Map<String, Part>.getAsType(key: String): T =
        T::class.java.cast(get(key))
            ?: throw IllegalArgumentException("Failed to retrieve and cast provided argument.")

    data class Args(
        val keyConfig: KeyManagementConfig?,
        val additionalAudiences: Set<AudienceKeyPair>,
        val objectStoreAddress: String,
        val storeRawBytes: Boolean,
        val id: String,
        val file: FilePart,
    )
}
