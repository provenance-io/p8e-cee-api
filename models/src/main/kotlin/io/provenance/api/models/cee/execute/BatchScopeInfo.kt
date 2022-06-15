package io.provenance.api.models.cee.execute

import java.util.UUID

data class BatchScopeInfo(
    val scopeUuid: UUID,
    val sessionUuid: UUID?,
    val records: Map<String, Any>,
)
