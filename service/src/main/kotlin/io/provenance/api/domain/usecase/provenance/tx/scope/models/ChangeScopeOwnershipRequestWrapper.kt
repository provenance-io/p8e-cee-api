package io.provenance.api.domain.usecase.provenance.tx.scope.models

import io.provenance.api.models.p8e.tx.ChangeScopeOwnershipBatchRequest
import io.provenance.api.models.p8e.tx.ChangeScopeOwnershipRequest
import java.util.UUID

data class ChangeScopeOwnershipRequestWrapper(
    val uuid: UUID,
    val request: ChangeScopeOwnershipRequest,
) {
    fun toBatchWrapper() = ChangeScopeOwnershipBatchRequestWrapper(
        uuid,
        ChangeScopeOwnershipBatchRequest(
            request.account,
            request.provenanceConfig,
            listOf(request.scopeId),
            request.newValueOwner,
            request.newDataAccess
        )
    )
}
