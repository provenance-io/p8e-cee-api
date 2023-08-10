package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.eos.permissions.RevokeObjectAccessRequest

data class RevokeObjectAccessRequestWrapper(
    val entity: Entity,
    val request: RevokeObjectAccessRequest
)
