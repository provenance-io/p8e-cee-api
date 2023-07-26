package io.provenance.api.domain.usecase.provenance.tx.permissions.authz.models

import io.provenance.api.models.entity.EntityID
import io.provenance.api.models.p8e.tx.permissions.authz.UpdateAuthzRequest

data class UpdateAuthzRequestWrapper(
    val entityID: EntityID,
    val request: UpdateAuthzRequest
)
