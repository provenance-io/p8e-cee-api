package io.provenance.api.domain.usecase.provenance.contracts.validationoracle

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.contracts.validationoracle.models.ValidationOracleQueryRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import org.springframework.stereotype.Component

@Component
class QueryValidationOracle(
    private val provenanceService: ProvenanceService,
) : AbstractUseCase<ValidationOracleQueryRequestWrapper, String>() {
    override suspend fun execute(args: ValidationOracleQueryRequestWrapper): String {
        return provenanceService.queryValidationOracle(args.request.provenanceConfig, args.request.contractConfig, args.request.libraryCall)
    }
}
