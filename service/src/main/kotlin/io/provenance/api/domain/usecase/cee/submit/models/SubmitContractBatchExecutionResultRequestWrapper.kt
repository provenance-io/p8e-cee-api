package io.provenance.api.domain.usecase.cee.submit.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.cee.submit.SubmitContractBatchExecutionResultRequest

data class SubmitContractBatchExecutionResultRequestWrapper(
    val entity: Entity,
    val request: SubmitContractBatchExecutionResultRequest
)
