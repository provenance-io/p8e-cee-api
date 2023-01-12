package io.provenance.api.models.cee.execute

import io.provenance.api.models.p8e.TxResponse
import java.util.UUID

sealed class ContractExecutionResponse(
    open val associatedScopeUuids: List<UUID>,
    open val error: String? = null,
    val multiparty: Boolean? = null,
)

data class SinglePartyContractExecutionResponse(
    val metadata: TxResponse?,
    override val error: String? = null,
    override val associatedScopeUuids: List<UUID>
) : ContractExecutionResponse(error = error, multiparty = false, associatedScopeUuids = associatedScopeUuids)

data class MultipartyContractExecutionResponse(
    val envelopeState: String?,
    override val error: String? = null,
    override val associatedScopeUuids: List<UUID>
) : ContractExecutionResponse(error = error, multiparty = true, associatedScopeUuids = associatedScopeUuids)

data class ContractExecutionErrorResponse(
    val type: String? = null,
    override val error: String,
    override val associatedScopeUuids: List<UUID>
) : ContractExecutionResponse(error = error, associatedScopeUuids = associatedScopeUuids)

data class ContractExecutionBatchResponse(
    val completed: List<ContractExecutionResponse>,
    val pending: List<ContractExecutionResponse>,
    val failed: List<ContractExecutionErrorResponse>
)
