package io.provenance.api.domain.usecase.cee.reject.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.cee.reject.RejectContractRequest

data class RejectContractExecutionRequestWrapper(
    val entity: Entity,
    val request: RejectContractRequest
)
