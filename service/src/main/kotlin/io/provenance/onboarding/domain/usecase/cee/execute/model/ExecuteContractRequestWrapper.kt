package io.provenance.onboarding.domain.usecase.cee.execute.model

import io.provenance.api.models.cee.ExecuteContractRequest
import java.util.UUID

data class ExecuteContractRequestWrapper(
    val uuid: UUID,
    val request: ExecuteContractRequest,
)
