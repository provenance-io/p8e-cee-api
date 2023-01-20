package io.provenance.api.models.cee.execute

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.provenance.api.models.p8e.TxResponse
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SinglePartyContractExecutionResponse::class, name = "single"),
    JsonSubTypes.Type(value = MultipartyContractExecutionResponse::class, name = "multi"),
    JsonSubTypes.Type(value = ContractExecutionErrorResponse::class, name = "error"),
)
sealed class ContractExecutionResponse(
    open val scopeUuids: List<UUID>,
    open val error: String? = null,
    val multiparty: Boolean? = null,
)

data class SinglePartyContractExecutionResponse(
    val metadata: TxResponse?,
    override val error: String? = null,
    override val scopeUuids: List<UUID>
) : ContractExecutionResponse(error = error, multiparty = false, scopeUuids = scopeUuids)

data class MultipartyContractExecutionResponse(
    val envelopeState: String?,
    override val error: String? = null,
    override val scopeUuids: List<UUID>
) : ContractExecutionResponse(error = error, multiparty = true, scopeUuids = scopeUuids)

data class ContractExecutionErrorResponse(
    val errorType: String? = null,
    override val error: String,
    override val scopeUuids: List<UUID>
) : ContractExecutionResponse(error = error, scopeUuids = scopeUuids)

data class ContractExecutionBatchResponse(
    val completed: List<ContractExecutionResponse>,
    val pending: List<ContractExecutionResponse>,
    val failed: List<ContractExecutionErrorResponse>
)
