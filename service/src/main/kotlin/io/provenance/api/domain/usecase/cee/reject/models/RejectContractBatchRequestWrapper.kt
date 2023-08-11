package io.provenance.api.domain.usecase.cee.reject.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.cee.reject.RejectContractBatchRequest

class RejectContractBatchRequestWrapper(
    val entity: Entity,
    val request: RejectContractBatchRequest,
)
