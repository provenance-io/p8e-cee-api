package io.provenance.api.domain.usecase.cee.submit.models

import io.provenance.api.models.cee.SubmitContractExecutionResultRequest
import java.util.UUID

data class SubmitContractExecutionResultRequestWrapper(
    val uuid: UUID,
    val request: SubmitContractExecutionResultRequest
)
