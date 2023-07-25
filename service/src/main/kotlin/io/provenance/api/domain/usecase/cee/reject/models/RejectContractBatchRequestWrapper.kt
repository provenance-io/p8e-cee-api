package io.provenance.api.domain.usecase.cee.reject.models

import io.provenance.api.models.user.EntityID
import io.provenance.api.models.cee.reject.RejectContractBatchRequest

class RejectContractBatchRequestWrapper(
    val entityID: EntityID,
    val request: RejectContractBatchRequest,
)
