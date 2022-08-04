package io.provenance.api.domain.usecase.objectStore.get.models

import io.provenance.api.models.eos.get.GetFileRequest
import java.util.UUID

data class GetFileRequestWrapper(
    val uuid: UUID,
    val request: GetFileRequest,
)
