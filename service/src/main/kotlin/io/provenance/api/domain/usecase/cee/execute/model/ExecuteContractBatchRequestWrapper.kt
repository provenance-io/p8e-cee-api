package io.provenance.api.domain.usecase.cee.execute.model

import io.provenance.api.models.user.EntityID
import io.provenance.api.models.cee.execute.ExecuteContractBatchRequest

data class ExecuteContractBatchRequestWrapper(
    val entityID: EntityID,
    val request: ExecuteContractBatchRequest,
)
