package io.provenance.api.models.cee.execute

import io.provenance.api.models.p8e.TxResponse

data class ContractExecutionResponse(
    val multiparty: Boolean,
    val envelopeState: String? = null,
    val metadata: TxResponse? = null,
)
