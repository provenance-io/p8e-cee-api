package io.provenance.api.domain.usecase.objectStore.store

import com.google.gson.Gson
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.objectStore.store.models.StoreFileRequestWrapper
import io.provenance.api.domain.usecase.objectStore.store.models.StoreObjectRequest
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.api.util.awaitAllBytes
import io.provenance.api.util.buildLogMessage
import io.provenance.entity.KeyType
import io.provenance.scope.util.toUuid
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.FormFieldPart
import org.springframework.http.codec.multipart.Part
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.Asset
import tech.figure.proto.util.FileNFT
import tech.figure.proto.util.toProtoAny
import tech.figure.proto.util.toProtoUUID

@Component
class StoreFile(
    private val entityManager: EntityManager,
    private val storeObject: StoreObject,
) : AbstractUseCase<StoreFileRequestWrapper, StoreProtoResponse>() {
    override suspend fun execute(args: StoreFileRequestWrapper): StoreProtoResponse =
        try {
            store(args)
        } finally {
            /*
             * The reactive multipart reader spills any part larger than its in-memory threshold to a
             * temporary file on disk. Those temp files are only removed when FilePart.delete() is
             * called, so we must delete every FilePart in the request on every exit path -- success,
             * business error, or a validation/cast failure raised from getParams(). Skipping this is
             * what caused /tmp/spring-multipart-* to grow unbounded over the lifetime of the pod.
             */
            args.request.deleteAllFileParts()
        }

    private suspend fun store(args: StoreFileRequestWrapper): StoreProtoResponse {
        val (account, permissions, objectStoreAddress, storeRawBytes, id, file, type) = getParams(args.request)
        val entity = entityManager.getEntity(KeyManagementConfigWrapper(args.entity.id, account?.keyManagementConfig))

        return file.awaitAllBytes().map { bytes ->
            storeObject.executeBlocking(
                StoreObjectRequest(
                    if (!storeRawBytes)
                        Asset.newBuilder().also {
                            it.id = id.toUuid().toProtoUUID()
                            it.type = FileNFT.ASSET_TYPE
                            it.description = file.filename()
                            it.putKv(FileNFT.KEY_FILENAME, file.filename().toProtoAny())
                            it.putKv(FileNFT.KEY_BYTES, bytes.toProtoAny())
                            it.putKv(FileNFT.KEY_SIZE, bytes.size.toString().toProtoAny())
                            it.putKv(FileNFT.KEY_CONTENT_TYPE, file.headers().contentType.toString().toProtoAny())
                        }.build().toByteArray() else bytes,
                    type,
                    objectStoreAddress,
                    args.useObjectStoreGateway,
                    entity.getKeyRef(KeyType.ENCRYPTION),
                    permissions,
                    account ?: AccountInfo()
                )
            )
        }.awaitSingle()
    }

    /**
     * Deletes the temporary file backing every [FilePart] in the request, ignoring parts that were
     * kept in memory or already removed. Errors are swallowed so cleanup never masks the original
     * outcome of the request.
     */
    private suspend fun Map<String, Part>.deleteAllFileParts() {
        val fileParts = values.filterIsInstance<FilePart>()
        if (fileParts.isEmpty()) {
            return
        }
        /*
         * Run inside NonCancellable so the temp files are still deleted when the request coroutine
         * was cancelled (e.g. the client aborted the upload mid-stream) -- otherwise the suspending
         * delete() call would immediately throw CancellationException and leak the file on disk.
         */
        withContext(NonCancellable) {
            fileParts.forEach { filePart ->
                runCatching { filePart.delete().awaitFirstOrNull() }
            }
        }
    }

    private fun getParams(request: Map<String, Part>): Args {
        var permissions: PermissionInfo? = null
        var account: AccountInfo? = null
        var type: String? = null

        request["account"]?.let {
            account = Gson().fromJson((it as FormFieldPart).value(), AccountInfo::class.java)
        }

        if (!request.containsKey("id") || request.getAsType<FormFieldPart>("id").value().isEmpty()) {
            throw IllegalArgumentException("Request must provide the 'id' field for the file")
        }

        request["permissions"]?.let {
            permissions = Gson().fromJson((it as FormFieldPart).value(), PermissionInfo::class.java)
        }

        request["type"]?.let {
            type = request.getAsType<FormFieldPart>("type").value()
        }

        val objectStoreAddress = request.getAsType<FormFieldPart>("objectStoreAddress").value()
        val storeRawBytes = request.getAsType<FormFieldPart>("storeRawBytes").value().toBoolean()
        val id = request.getAsType<FormFieldPart>("id").value()
        val file = request.getAsType<FilePart>("file")
        return Args(account, permissions, objectStoreAddress, storeRawBytes, id, file, type)
    }

    private inline fun <reified T> Map<String, Part>.getAsType(key: String): T =
        T::class.java.cast(get(key))
            ?: throw IllegalArgumentException(
                buildLogMessage(
                    "Failed to retrieve and cast provided argument",
                    "javaClass" to T::class.java.name,
                )
            )

    data class Args(
        val account: AccountInfo?,
        val permissions: PermissionInfo?,
        val objectStoreAddress: String,
        val storeRawBytes: Boolean,
        val id: String,
        val file: FilePart,
        val type: String?,
    )
}
