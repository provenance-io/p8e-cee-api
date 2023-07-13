package io.provenance.api.domain.usecase.cee.reject.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.cee.reject.RejectContractBatchRequest

class RejectContractBatchRequestWrapper(
    val userID: UserID,
    val request: RejectContractBatchRequest,
)
