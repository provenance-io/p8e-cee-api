package io.provenance.api.domain.usecase.common.model

import java.util.UUID

data class ScopeConfig(
    val scopeId: UUID,
    val contractSpecId: UUID,
    val scopeSpecId: UUID,
)
