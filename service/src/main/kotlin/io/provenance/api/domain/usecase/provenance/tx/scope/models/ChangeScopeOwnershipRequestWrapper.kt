package io.provenance.api.domain.usecase.provenance.tx.scope.models

import io.provenance.api.models.p8e.tx.ChangeScopeOwnershipBatchRequest
import io.provenance.api.models.p8e.tx.ChangeScopeOwnershipRequest
import io.provenance.api.models.entity.Entity

data class ChangeScopeOwnershipRequestWrapper(
    val entity: Entity,
    val request: ChangeScopeOwnershipRequest,
) {
    fun toBatchWrapper() = ChangeScopeOwnershipBatchRequestWrapper(
        entity,
        ChangeScopeOwnershipBatchRequest(
            request.account,
            request.provenanceConfig,
            listOf(request.scopeId),
            request.newValueOwner,
            request.newDataAccess
        ),
    )
}
