package io.provenance.api.domain.usecase.provenance.tx.permissions.dataAccess.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.p8e.tx.permissions.dataAccess.UpdateScopeDataAccessRequest

data class UpdateScopeDataAccessRequestWrapper(
    val entity: Entity,
    val request: UpdateScopeDataAccessRequest
)
