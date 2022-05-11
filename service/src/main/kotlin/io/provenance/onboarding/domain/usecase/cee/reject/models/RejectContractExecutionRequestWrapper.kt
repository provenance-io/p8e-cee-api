package io.provenance.onboarding.domain.usecase.cee.reject.models

import io.provenance.api.models.cee.RejectContractRequest
import java.util.UUID

data class RejectContractExecutionRequestWrapper(
    val uuid: UUID,
    val request: RejectContractRequest
)
