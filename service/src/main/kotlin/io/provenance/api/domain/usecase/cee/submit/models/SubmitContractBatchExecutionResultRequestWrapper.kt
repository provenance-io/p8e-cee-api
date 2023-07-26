package io.provenance.api.domain.usecase.cee.submit.models

import io.provenance.api.models.entity.EntityID
import io.provenance.api.models.cee.submit.SubmitContractBatchExecutionResultRequest

data class SubmitContractBatchExecutionResultRequestWrapper(
    val entityID: EntityID,
    val request: SubmitContractBatchExecutionResultRequest
)
