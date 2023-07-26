package io.provenance.api.domain.usecase.cee.approve.models

import io.provenance.api.models.entity.EntityID
import io.provenance.api.models.cee.approve.ApproveContractBatchRequest

data class ApproveContractBatchRequestWrapper(
    val entityID: EntityID,
    val request: ApproveContractBatchRequest
)
