package io.provenance.api.models.cee.execute

import io.provenance.api.models.p8e.TxResponse

sealed class ContractExecutionResponse(
    open val error: String? = null,
    val multiparty: Boolean? = null,
)

data class SinglePartyContractExecutionResponse(
    val metadata: TxResponse?,
    override val error: String? = null
) : ContractExecutionResponse(error = error, multiparty = false)

data class MultipartyContractExecutionResponse(
    val envelopeState: String?,
    override val error: String? = null
) : ContractExecutionResponse(error = error, multiparty = true)

data class ContractExecutionErrorResponse(
    override val error: String
) : ContractExecutionResponse(error = error)
