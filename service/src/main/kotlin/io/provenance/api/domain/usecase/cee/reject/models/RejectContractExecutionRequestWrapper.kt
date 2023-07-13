package io.provenance.api.domain.usecase.cee.reject.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.cee.reject.RejectContractRequest

data class RejectContractExecutionRequestWrapper(
    val userID: UserID,
    val request: RejectContractRequest
)
