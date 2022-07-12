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

@Component
class StoreFile(
    private val objectStore: ObjectStore,
    private val objectStoreConfig: ObjectStoreConfig,
    private val entityManager: EntityManager,
) : AbstractUseCase<StoreFileRequestWrapper, StoreProtoResponse>() {
    override suspend fun execute(args: StoreFileRequestWrapper): StoreProtoResponse {
        var additionalAudiences = emptySet<AudienceKeyPair>()
        var keyConfig: KeyManagementConfig? = null
        args.request["account"]?.let {
            keyConfig = Gson().fromJson((it as FormFieldPart).value(), AccountInfo::class.java).keyManagementConfig
        }

        if (!args.request.containsKey("id") || args.request.getAsType<FormFieldPart>("id").value().isEmpty()) {
            throw IllegalArgumentException("Request must provide the 'id' field for the file")
        }

        args.request["permissions"]?.let {
            val permissions = Gson().fromJson((it as FormFieldPart).value(), PermissionInfo::class.java)
            additionalAudiences = entityManager.hydrateKeys(permissions)
        }

        val originator = entityManager.getEntity(KeyManagementConfigWrapper(args.uuid, keyConfig))
        val file = args.request.getAsType<FilePart>("file")

        OsClient(
            URI.create(args.request.getAsType<FormFieldPart>("objectStoreAddress").value()),
            objectStoreConfig.timeoutMs,
        ).use { osClient ->
            return objectStore.storeFile(
                osClient,
                ByteArrayInputStream(file.awaitAllBytes()),
                originator.encryptionPublicKey() as PublicKey,
                additionalAudiences.map { it.encryptionKey.toJavaPublicKey() }.toSet(),
            )
        }
    }

    private inline fun <reified T> Map<String, Part>.getAsType(key: String): T =
        T::class.java.cast(get(key))
            ?: throw IllegalArgumentException("Failed to retrieve and cast provided argument.")
}
