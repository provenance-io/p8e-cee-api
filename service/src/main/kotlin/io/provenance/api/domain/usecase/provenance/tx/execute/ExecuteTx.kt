package io.provenance.api.domain.usecase.provenance.tx.execute

import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.tx.execute.models.ExecuteTxRequestWrapper
import io.provenance.api.models.p8e.TxResponse
import org.springframework.stereotype.Component

@Component
class ExecuteTx(
    private val provenance: Provenance,
    private val getSigner: GetSigner,
) : AbstractUseCase<ExecuteTxRequestWrapper, TxResponse>() {
    override suspend fun execute(args: ExecuteTxRequestWrapper): TxResponse {
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.account))
        return provenance.onboard(args.request.chainId, args.request.nodeEndpoint, signer, args.request.tx)
    }
}
