package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.user.EntityID
import io.provenance.api.models.eos.permissions.RegisterScopeObjectsAccessRequest

data class RegisterScopeObjectsAccessRequestWrapper(
    val entityID: EntityID,
    val request: RegisterScopeObjectsAccessRequest
)
