package io.provenance.api.domain.usecase.provenance.query

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.query.QueryScopeRequest
import io.provenance.metadata.v1.ScopeResponse
import org.springframework.stereotype.Component

@Component
class QueryScope(
    private val provenanceService: ProvenanceService,
) : AbstractUseCase<QueryScopeRequest, ScopeResponse>() {
    override suspend fun execute(args: QueryScopeRequest): ScopeResponse =
        provenanceService.getScope(ProvenanceConfig(args.chainId, args.nodeEndpoint), args.scopeUuid, args.height)
}
