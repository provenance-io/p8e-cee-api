package io.provenance.api.domain.usecase.cee.submit.models

import io.provenance.api.models.cee.submit.SubmitContractBatchExecutionResultRequest
import java.util.UUID

data class SubmitContractBatchExecutionResultRequestWrapper(
    val uuid: UUID,
    val request: SubmitContractBatchExecutionResultRequest
)
