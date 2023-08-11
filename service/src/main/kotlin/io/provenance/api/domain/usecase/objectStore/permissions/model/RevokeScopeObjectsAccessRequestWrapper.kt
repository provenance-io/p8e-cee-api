package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.eos.permissions.RevokeScopeObjectsAccessRequest
import io.provenance.api.models.entity.Entity

data class RevokeScopeObjectsAccessRequestWrapper(
    val entity: Entity,
    val request: RevokeScopeObjectsAccessRequest
)
