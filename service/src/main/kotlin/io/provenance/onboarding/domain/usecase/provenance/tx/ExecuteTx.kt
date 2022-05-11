package io.provenance.onboarding.domain.usecase.provenance.tx

import io.provenance.api.models.p8e.TxResponse
import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.provenance.account.GetSigner
import io.provenance.onboarding.domain.usecase.provenance.tx.model.ExecuteTxRequest
import org.springframework.stereotype.Component

@Component
class ExecuteTx(
    private val provenance: Provenance,
    private val getSigner: GetSigner,
) : AbstractUseCase<ExecuteTxRequest, TxResponse>() {
    override suspend fun execute(args: ExecuteTxRequest): TxResponse {
        val signer = getSigner.execute(args.account)
        return provenance.onboard(args.chainId, args.nodeEndpoint, signer, args.tx)
    }
}
