package io.provenance.api.domain.usecase.provenance.query

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.query.models.QueryScopeRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.util.toPrettyJson
import org.springframework.stereotype.Component

@Component
class QueryScope(
    private val provenanceService: ProvenanceService,
//    private val createClient: CreateClient
) : AbstractUseCase<QueryScopeRequestWrapper, Unit>() {
    override suspend fun execute(args: QueryScopeRequestWrapper) {
//        val client = createClient.execute(CreateClientRequest(args.uuid, args.request.account, args.request.client))
        val scope = provenanceService.getScope(args.request.provenanceConfig, args.request.scopeUuid, args.request.height)
        println(scope.toPrettyJson())
    }
}
