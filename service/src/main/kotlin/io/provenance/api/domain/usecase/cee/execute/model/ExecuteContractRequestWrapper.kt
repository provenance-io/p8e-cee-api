package io.provenance.api.domain.usecase.cee.execute.model

import io.provenance.api.models.entity.EntityID
import io.provenance.api.models.cee.execute.ExecuteContractRequest

data class ExecuteContractRequestWrapper(
    val entityID: EntityID,
    val request: ExecuteContractRequest,
)
