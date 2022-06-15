package io.provenance.api.domain.usecase.cee.reject.models

import io.provenance.api.models.cee.reject.RejectContractRequest
import java.util.UUID

data class RejectContractExecutionRequestWrapper(
    val uuid: UUID,
    val request: RejectContractRequest
)
