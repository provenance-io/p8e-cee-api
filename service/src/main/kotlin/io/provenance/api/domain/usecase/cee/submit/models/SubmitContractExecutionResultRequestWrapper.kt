package io.provenance.api.domain.usecase.cee.submit.models

import io.provenance.api.models.entity.EntityID
import io.provenance.api.models.cee.submit.SubmitContractExecutionResultRequest

data class SubmitContractExecutionResultRequestWrapper(
    val entityID: EntityID,
    val request: SubmitContractExecutionResultRequest
)
