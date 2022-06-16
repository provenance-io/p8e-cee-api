package io.provenance.api.domain.usecase.cee.approve.models

import io.provenance.api.models.cee.approve.ApproveContractBatchRequest
import java.util.UUID

data class ApproveContractBatchRequestWrapper(
    val uuid: UUID,
    val request: ApproveContractBatchRequest
)
