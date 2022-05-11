package io.provenance.onboarding.domain.usecase.provenance.tx

import io.provenance.api.models.p8e.TxResponse
import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.provenance.account.GetSigner
import io.provenance.onboarding.domain.usecase.provenance.tx.model.ExecuteTxRequestWrapper
import org.springframework.stereotype.Component

@Component
class ExecuteTx(
    private val provenance: Provenance,
    private val getSigner: GetSigner,
) : AbstractUseCase<ExecuteTxRequestWrapper, TxResponse>() {
    override suspend fun execute(args: ExecuteTxRequestWrapper): TxResponse {
        val signer = getSigner.execute(args.uuid)
        return provenance.onboard(args.request.chainId, args.request.nodeEndpoint, signer, args.request.tx)
    }
}
