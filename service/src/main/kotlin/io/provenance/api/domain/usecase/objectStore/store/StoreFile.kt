package io.provenance.api.domain.usecase.objectStore.store

import com.google.gson.Gson
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.objectStore.store.models.StoreFileRequestWrapper
import io.provenance.api.domain.usecase.objectStore.store.models.StoreObjectRequest
import io.provenance.api.domain.usecase.objectStore.store.models.StoreObjectStreamRequest
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.api.util.awaitAllBytes
import io.provenance.api.util.buildLogMessage
import io.provenance.api.util.transferToPath
import io.provenance.entity.KeyType
import io.provenance.scope.encryption.model.KeyRef
import io.provenance.scope.util.toUuid
import java.nio.file.Files
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.FormFieldPart
import org.springframework.http.codec.multipart.Part
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
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
        val params = getParams(args.request)
        val entity = entityManager.getEntity(KeyManagementConfigWrapper(args.entity.id, params.account?.keyManagementConfig))
        val keyRef = entity.getKeyRef(KeyType.ENCRYPTION)

        /*
         * Fast path for large raw uploads: when the caller wants the bytes stored verbatim (no Asset
         * proto wrapping) and is not going through the gateway, stream the file straight from disk to
         * the object store. This avoids loading the entire (potentially ~1GB) payload into a heap
         * ByteArray, which is the dominant driver of the OutOfMemory errors on this endpoint. The
         * proto-wrapping and gateway paths inherently need the bytes in memory and are left unchanged.
         */
        if (params.storeRawBytes && !args.useObjectStoreGateway) {
            return streamRawBytes(params, keyRef)
        }

        return params.file.awaitAllBytes().map { bytes ->
            storeObject.executeBlocking(
                StoreObjectRequest(
                    if (!params.storeRawBytes) {
                        Asset.newBuilder().also {
                            it.id = params.id.toUuid().toProtoUUID()
                            it.type = FileNFT.ASSET_TYPE
                            it.description = params.file.filename()
                            it.putKv(FileNFT.KEY_FILENAME, params.file.filename().toProtoAny())
                            it.putKv(FileNFT.KEY_BYTES, bytes.toProtoAny())
                            it.putKv(FileNFT.KEY_SIZE, bytes.size.toString().toProtoAny())
                            it.putKv(FileNFT.KEY_CONTENT_TYPE, params.file.headers().contentType.toString().toProtoAny())
                        }.build().toByteArray()
                    } else {
                        bytes
                    },
                    params.type,
                    params.objectStoreAddress,
                    args.useObjectStoreGateway,
                    keyRef,
                    params.permissions,
                    params.account ?: AccountInfo()
                )
            )
        }.awaitSingle()
    }

    /*
     * Materializes ("Spills") the part to a dedicated temp file, streams it to the object store, then always
     * removes that temp file. Transferring to our own file (rather than reading the part's reactive
     * content() into memory) lets us hand `OsClient.put` a plain InputStream together with an
     * authoritative content length taken from `Files.size()`, the real number of bytes on disk —
     * not a client-supplied per-part Content-Length header, which multipart parts frequently omit.
     * The reader-owned spill file is cleaned up separately via deleteAllFileParts().
     */
    private suspend fun streamRawBytes(params: Args, keyRef: KeyRef): StoreProtoResponse {
        val destination = Files.createTempFile("p8e-cee-stream-", ".upload")
        return try {
            params.file.transferToPath(destination).awaitSingle()
            storeObject.executeStreaming(
                StoreObjectStreamRequest(
                    destination,
                    Files.size(destination),
                    params.type,
                    params.objectStoreAddress,
                    keyRef,
                    params.permissions,
                    params.account ?: AccountInfo()
                )
            )
        } finally {
            withContext(NonCancellable) {
                runCatching { Files.deleteIfExists(destination) }
            }
        }
    }

    /**
     * Deletes the temporary file backing every [FilePart] in the request, ignoring parts that were
     * kept in memory or already removed. Errors are swallowed so cleanup never masks the original
     * outcome of the request.
     *
     * Iterates every value across all field names (not the single-value view) so that duplicate
     * parts sharing a field name -- which the reader still spills to disk -- are also cleaned up.
     */
    private suspend fun MultiValueMap<String, Part>.deleteAllFileParts() {
        val fileParts = values.flatten().filterIsInstance<FilePart>()
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

    private fun getParams(request: MultiValueMap<String, Part>): Args {
        var permissions: PermissionInfo? = null
        var account: AccountInfo? = null
        var type: String? = null

        request.getFirst("account")?.let {
            account = Gson().fromJson((it as FormFieldPart).value(), AccountInfo::class.java)
        }

        if (!request.containsKey("id") || request.getAsType<FormFieldPart>("id").value().isEmpty()) {
            throw IllegalArgumentException("Request must provide the 'id' field for the file")
        }

        request.getFirst("permissions")?.let {
            permissions = Gson().fromJson((it as FormFieldPart).value(), PermissionInfo::class.java)
        }

        request.getFirst("type")?.let {
            type = request.getAsType<FormFieldPart>("type").value()
        }

        val objectStoreAddress = request.getAsType<FormFieldPart>("objectStoreAddress").value()
        val storeRawBytes = request.getAsType<FormFieldPart>("storeRawBytes").value().toBoolean()
        val id = request.getAsType<FormFieldPart>("id").value()
        val file = request.getAsType<FilePart>("file")
        return Args(account, permissions, objectStoreAddress, storeRawBytes, id, file, type)
    }

    private inline fun <reified T> MultiValueMap<String, Part>.getAsType(key: String): T =
        T::class.java.cast(getFirst(key))
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
