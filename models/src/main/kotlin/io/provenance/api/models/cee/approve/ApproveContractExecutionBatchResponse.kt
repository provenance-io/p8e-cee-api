package io.provenance.api.models.cee.approve

import java.util.UUID

data class ApproveContractExecutionErrorResponse(
    val type: String,
    val error: String,
    val scopeUuids: List<UUID>
)

data class ApproveContractExecutionBatchResponse(
    val completed: List<ApproveContractExecutionResponse>,
    val failed: List<ApproveContractExecutionErrorResponse>
)
