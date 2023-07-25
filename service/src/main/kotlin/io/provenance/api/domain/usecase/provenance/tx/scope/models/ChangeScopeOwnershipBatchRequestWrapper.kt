package io.provenance.api.domain.usecase.provenance.tx.scope.models

import io.provenance.api.models.user.EntityID
import io.provenance.api.models.p8e.tx.ChangeScopeOwnershipBatchRequest

data class ChangeScopeOwnershipBatchRequestWrapper(
    val entityID: EntityID,
    val request: ChangeScopeOwnershipBatchRequest,
)
