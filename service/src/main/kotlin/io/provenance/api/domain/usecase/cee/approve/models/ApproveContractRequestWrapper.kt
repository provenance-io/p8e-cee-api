package io.provenance.api.domain.usecase.cee.approve.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.cee.approve.ApproveContractRequest

data class ApproveContractRequestWrapper(
    val userID: UserID,
    val request: ApproveContractRequest
)
