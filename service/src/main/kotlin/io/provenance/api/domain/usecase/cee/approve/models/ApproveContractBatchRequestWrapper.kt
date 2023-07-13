package io.provenance.api.domain.usecase.cee.approve.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.cee.approve.ApproveContractBatchRequest

data class ApproveContractBatchRequestWrapper(
    val userID: UserID,
    val request: ApproveContractBatchRequest
)
