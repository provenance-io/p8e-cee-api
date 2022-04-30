package io.provenance.onboarding.domain.usecase.cee.execute.model

import java.util.UUID

data class ContractConfig(
    val contractName: String,
    val scopeUuid: UUID,
    val sessionUuid: UUID?,
)
