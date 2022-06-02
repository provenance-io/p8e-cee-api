package io.provenance.api.domain.usecase.provenance.query

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.query.models.QueryScopeRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.metadata.v1.ScopeResponse
import org.springframework.stereotype.Component

@Component
class QueryScope(
    private val provenanceService: ProvenanceService,
) : AbstractUseCase<QueryScopeRequestWrapper, ScopeResponse>() {
    override suspend fun execute(args: QueryScopeRequestWrapper): ScopeResponse =
        provenanceService.getScope(args.request.provenanceConfig, args.request.scopeUuid, args.request.height)
}
