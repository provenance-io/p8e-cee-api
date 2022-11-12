package io.provenance.api.domain.usecase.provenance.contracts.validationoracle

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.contracts.validationoracle.models.SmartContractTransactionRequestWrapper
import io.provenance.api.frameworks.provenance.SmartContractService
import io.provenance.api.models.p8e.TxResponse
import org.springframework.stereotype.Component

@Component
class ExecuteSmartContract(
    private val smartContractService: SmartContractService,
    private val getSigner: GetSigner
) : AbstractUseCase<SmartContractTransactionRequestWrapper, TxResponse>() {
    override suspend fun execute(args: SmartContractTransactionRequestWrapper): TxResponse {
        val signer = getSigner.execute(
            GetSignerRequest(
                args.uuid,
                args.request.account,
            )
        )
        return smartContractService.executeSmartContractTransaction(args.request.provenanceConfig, signer, args.request.contractConfig, args.request.libraryInvocation)
    }
}
