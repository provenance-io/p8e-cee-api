package io.provenance.api.models.cee

import java.util.UUID

data class ContractConfig(
    val contractName: String,
    val scopeUuid: UUID,
    val sessionUuid: UUID?,
    val scopeSpecificationName: String,
    val messageParser: String?,
)
