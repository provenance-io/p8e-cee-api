package io.provenance.api.domain.usecase.provenance.tx.scope.models

import io.provenance.api.models.p8e.tx.ChangeScopeOwnershipBatchRequest
import io.provenance.api.models.p8e.tx.ChangeScopeOwnershipRequest
import io.provenance.api.models.user.UserID

data class ChangeScopeOwnershipRequestWrapper(
    val userID: UserID,
    val request: ChangeScopeOwnershipRequest,
) {
    fun toBatchWrapper() = ChangeScopeOwnershipBatchRequestWrapper(
        userID,
        ChangeScopeOwnershipBatchRequest(
            request.account,
            request.provenanceConfig,
            listOf(request.scopeId),
            request.newValueOwner,
            request.newDataAccess
        )
    )
}
