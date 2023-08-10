package io.provenance.api.domain.usecase.provenance.tx.scope.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.p8e.tx.ChangeScopeOwnershipBatchRequest

data class ChangeScopeOwnershipBatchRequestWrapper(
    val entity: Entity,
    val request: ChangeScopeOwnershipBatchRequest,
)
