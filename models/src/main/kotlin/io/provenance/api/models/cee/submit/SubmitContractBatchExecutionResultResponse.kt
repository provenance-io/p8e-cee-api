package io.provenance.api.models.cee.submit

import io.provenance.api.models.p8e.TxResponse

data class SubmitContractBatchExecutionResultResponse(
    val tx: TxResponse?,
    val error: String? = null
)
