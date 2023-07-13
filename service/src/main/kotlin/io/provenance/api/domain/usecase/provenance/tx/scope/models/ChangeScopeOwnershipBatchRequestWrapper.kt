package io.provenance.api.domain.usecase.provenance.tx.scope.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.p8e.tx.ChangeScopeOwnershipBatchRequest

data class ChangeScopeOwnershipBatchRequestWrapper(
    val userID: UserID,
    val request: ChangeScopeOwnershipBatchRequest,
)
