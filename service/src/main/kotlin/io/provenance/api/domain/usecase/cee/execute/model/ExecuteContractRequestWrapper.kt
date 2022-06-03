package io.provenance.api.domain.usecase.cee.execute.model

import io.provenance.api.models.cee.execute.ExecuteContractRequest
import java.util.UUID

data class ExecuteContractRequestWrapper(
    val uuid: UUID,
    val request: ExecuteContractRequest,
)
