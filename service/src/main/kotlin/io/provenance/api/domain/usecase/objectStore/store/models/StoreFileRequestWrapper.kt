package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.entity.Entity
import java.util.UUID
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.util.MultiValueMap

data class StoreFileRequestWrapper(
    val entity: Entity,
    /*
     * The full multipart map (all values per field name), not the single-value view. Retaining every
     * part -- including duplicates that share a field name -- is required so cleanup can delete the
     * temp file backing each spilled FilePart. toSingleValueMap() keeps only the first part per name,
     * which silently dropped (and therefore leaked the temp files of) any duplicate-named parts.
     */
    val request: MultiValueMap<String, Part>,
    val useObjectStoreGateway: Boolean = false
)

data class SwaggerStoreFileRequestWrapper(
    val objectStoreAddress: String,
    val id: UUID,
    val file: FilePart,
    val storeRawBytes: Boolean,
)

data class SwaggerGetFileResponse(
    val value: ByteArray
)
