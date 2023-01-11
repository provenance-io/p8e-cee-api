package io.provenance.api.models.cee.approve

data class ApproveContractExecutionErrorResponse(
    val type: String,
    val error: String
)

data class ApproveContractExecutionBatchResponse(
    val completed: List<ApproveContractExecutionResponse>,
    val failed: List<ApproveContractExecutionErrorResponse>
)
