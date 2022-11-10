package io.provenance.api.domain.usecase.provenance.contracts.validationoracle

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.contracts.validationoracle.models.ValidationOracleTransactionRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.models.p8e.TxResponse
import org.springframework.stereotype.Component

@Component
class ValidationOracleTransaction(
    private val provenanceService: ProvenanceService,
    private val getSigner: GetSigner
) : AbstractUseCase<ValidationOracleTransactionRequestWrapper, TxResponse>() {
    override suspend fun execute(args: ValidationOracleTransactionRequestWrapper): TxResponse {
        val signer = getSigner.execute(
            GetSignerRequest(
                args.uuid,
                args.request.account,
            )
        )
        return provenanceService.executeValidationOracleTransaction(args.request.provenanceConfig, signer, args.request.contractConfig, args.request.libraryCall)
    }
}
