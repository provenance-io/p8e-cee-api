package io.provenance.api.domain.usecase.cee.approve.models

import io.provenance.api.models.cee.ApproveContractRequest
import java.util.UUID

data class ApproveContractRequestWrapper(
    val uuid: UUID,
    val request: ApproveContractRequest
)
