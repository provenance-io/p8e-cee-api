package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.eos.permissions.RevokeScopeObjectsAccessRequest
import io.provenance.api.models.entity.EntityID

data class RevokeScopeObjectsAccessRequestWrapper(
    val entityID: EntityID,
    val request: RevokeScopeObjectsAccessRequest
)
