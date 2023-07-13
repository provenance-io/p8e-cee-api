package io.provenance.api.domain.usecase.cee.submit.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.cee.submit.SubmitContractBatchExecutionResultRequest

data class SubmitContractBatchExecutionResultRequestWrapper(
    val userID: UserID,
    val request: SubmitContractBatchExecutionResultRequest
)
