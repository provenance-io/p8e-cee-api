package io.provenance.api.domain.usecase.cee.execute.model

import io.provenance.api.models.user.UserID
import io.provenance.api.models.cee.execute.ExecuteContractBatchRequest

data class ExecuteContractBatchRequestWrapper(
    val userID: UserID,
    val request: ExecuteContractBatchRequest,
)
