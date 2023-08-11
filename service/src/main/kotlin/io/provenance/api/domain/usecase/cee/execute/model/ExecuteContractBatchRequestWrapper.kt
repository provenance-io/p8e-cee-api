package io.provenance.api.domain.usecase.cee.execute.model

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.cee.execute.ExecuteContractBatchRequest

data class ExecuteContractBatchRequestWrapper(
    val entity: Entity,
    val request: ExecuteContractBatchRequest,
)
