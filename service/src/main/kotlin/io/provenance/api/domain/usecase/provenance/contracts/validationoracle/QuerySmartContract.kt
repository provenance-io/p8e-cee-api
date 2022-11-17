package io.provenance.api.domain.usecase.provenance.contracts.validationoracle

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.contracts.validationoracle.models.SmartContractQueryRequestWrapper
import io.provenance.api.frameworks.smartcontract.SmartContractService
import org.springframework.stereotype.Component

@Component
class QuerySmartContract(
    private val smartContractService: SmartContractService,
) : AbstractUseCase<SmartContractQueryRequestWrapper, String>() {
    override suspend fun execute(args: SmartContractQueryRequestWrapper): String {
        return smartContractService.querySmartContract(args.request.provenanceConfig, args.request.contractConfig, args.request.libraryInvocation)
    }
}
