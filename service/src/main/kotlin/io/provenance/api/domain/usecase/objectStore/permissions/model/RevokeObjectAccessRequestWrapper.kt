package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.entity.EntityID
import io.provenance.api.models.eos.permissions.RevokeObjectAccessRequest

data class RevokeObjectAccessRequestWrapper(
    val entityID: EntityID,
    val request: RevokeObjectAccessRequest
)
