package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.entity.Entity
import java.util.UUID
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part

data class StoreFileRequestWrapper(
    val entity: Entity,
    val request: Map<String, Part>,
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
