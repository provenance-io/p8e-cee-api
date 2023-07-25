package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.eos.permissions.RegisterObjectAccessRequest
import io.provenance.api.models.user.EntityID

data class RegisterObjectAccessRequestWrapper(
    val entityID: EntityID,
    val request: RegisterObjectAccessRequest
)
