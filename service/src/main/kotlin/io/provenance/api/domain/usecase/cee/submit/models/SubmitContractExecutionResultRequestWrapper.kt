package io.provenance.api.domain.usecase.cee.submit.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.cee.submit.SubmitContractExecutionResultRequest

data class SubmitContractExecutionResultRequestWrapper(
    val entity: Entity,
    val request: SubmitContractExecutionResultRequest
)
