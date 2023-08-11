package io.provenance.api.domain.usecase.cee.execute.model

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.cee.execute.ExecuteContractRequest

data class ExecuteContractRequestWrapper(
    val entity: Entity,
    val request: ExecuteContractRequest,
)
