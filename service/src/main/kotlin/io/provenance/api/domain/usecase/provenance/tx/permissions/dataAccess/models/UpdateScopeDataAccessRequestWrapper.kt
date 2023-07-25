package io.provenance.api.domain.usecase.provenance.tx.permissions.dataAccess.models

import io.provenance.api.models.user.EntityID
import io.provenance.api.models.p8e.tx.permissions.dataAccess.UpdateScopeDataAccessRequest

data class UpdateScopeDataAccessRequestWrapper(
    val entityID: EntityID,
    val request: UpdateScopeDataAccessRequest
)
