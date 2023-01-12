package io.provenance.api.models.cee.submit

import io.provenance.api.models.p8e.TxResponse
import java.util.UUID

data class SubmitContractBatchExecutionResultResponse(
    val tx: TxResponse?,
    val error: String? = null,
    val associatedScopeUuids: List<UUID>,
)

data class SubmitContractBatchErrorResponse(
    val type: String,
    val error: String,
    val associatedScopeUuids: List<UUID>,
)

data class SubmitContractBatchExecutionResponseWrapper(
    val completed: List<SubmitContractBatchExecutionResultResponse>,
    val failed: List<SubmitContractBatchErrorResponse>
)
