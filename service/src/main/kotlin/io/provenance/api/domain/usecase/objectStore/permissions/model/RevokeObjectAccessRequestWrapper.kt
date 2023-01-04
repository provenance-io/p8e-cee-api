package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.eos.permissions.RevokeObjectAccessRequest
import java.util.UUID

data class RevokeObjectAccessRequestWrapper(
    val uuid: UUID,
    val request: RevokeObjectAccessRequest
)
