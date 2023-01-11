package io.provenance.api.models.cee.submit

import io.provenance.api.models.p8e.TxResponse

data class SubmitContractBatchExecutionResultResponse(
    val tx: TxResponse?,
    val error: String? = null
)

data class SubmitContractBatchErrorResponse(
    val type: String,
    val error: String
)

data class SubmitContractBatchExecutionResponseWrapper(
    val completed: List<SubmitContractBatchExecutionResultResponse>,
    val failed: List<SubmitContractBatchErrorResponse>
)
