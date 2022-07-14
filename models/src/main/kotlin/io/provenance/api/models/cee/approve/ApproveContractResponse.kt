package io.provenance.api.models.cee.approve

import io.provenance.api.models.p8e.TxResponse

data class ApproveContractResponse(
    val envelopeState: String,
    val tx: TxResponse
)
