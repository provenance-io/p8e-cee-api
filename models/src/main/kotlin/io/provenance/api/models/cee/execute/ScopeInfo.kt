package io.provenance.api.models.cee.execute

import java.util.UUID

data class ScopeInfo(
    val scopeUuid: UUID,
    val sessionUuid: UUID?,
)
