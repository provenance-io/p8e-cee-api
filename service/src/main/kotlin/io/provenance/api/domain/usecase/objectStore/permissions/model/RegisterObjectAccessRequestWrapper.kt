package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.eos.permissions.RegisterObjectAccessRequest
import io.provenance.api.models.entity.Entity

data class RegisterObjectAccessRequestWrapper(
    val entity: Entity,
    val request: RegisterObjectAccessRequest
)
