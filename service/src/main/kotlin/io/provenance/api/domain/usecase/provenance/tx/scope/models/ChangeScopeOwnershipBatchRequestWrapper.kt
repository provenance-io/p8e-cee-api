package io.provenance.api.domain.usecase.provenance.tx.scope.models

import io.provenance.api.models.p8e.tx.ChangeScopeOwnershipBatchRequest
import java.util.UUID

data class ChangeScopeOwnershipBatchRequestWrapper(
    val uuid: UUID,
    val request: ChangeScopeOwnershipBatchRequest,
)
