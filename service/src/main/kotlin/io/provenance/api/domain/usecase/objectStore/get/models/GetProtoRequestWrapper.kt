package io.provenance.api.domain.usecase.objectStore.get.models

import io.provenance.api.models.eos.get.GetProtoRequest
import java.util.UUID

data class GetProtoRequestWrapper(
    val uuid: UUID,
    val request: GetProtoRequest
)
