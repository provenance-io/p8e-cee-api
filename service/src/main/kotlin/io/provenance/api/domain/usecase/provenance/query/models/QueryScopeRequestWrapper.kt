package io.provenance.api.domain.usecase.provenance.query.models

import io.provenance.api.models.p8e.query.QueryScopeRequest
import java.util.UUID

data class QueryScopeRequestWrapper(
    val uuid: UUID,
    val request: QueryScopeRequest,
)
