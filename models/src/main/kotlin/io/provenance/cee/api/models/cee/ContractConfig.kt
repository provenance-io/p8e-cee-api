package io.provenance.cee.api.models.cee

import java.util.UUID

data class ContractConfig(
    val contractName: String,
    val scopeUuid: UUID,
    val sessionUuid: UUID?,
)
