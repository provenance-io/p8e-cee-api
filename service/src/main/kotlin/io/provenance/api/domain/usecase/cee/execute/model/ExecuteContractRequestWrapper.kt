package io.provenance.api.domain.usecase.cee.execute.model

import io.provenance.api.models.user.UserID
import io.provenance.api.models.cee.execute.ExecuteContractRequest

data class ExecuteContractRequestWrapper(
    val userID: UserID,
    val request: ExecuteContractRequest,
)
