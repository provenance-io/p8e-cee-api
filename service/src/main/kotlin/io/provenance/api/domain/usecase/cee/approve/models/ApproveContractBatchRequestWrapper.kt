package io.provenance.api.domain.usecase.cee.approve.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.cee.approve.ApproveContractBatchRequest

data class ApproveContractBatchRequestWrapper(
    val entity: Entity,
    val request: ApproveContractBatchRequest
)
