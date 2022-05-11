package io.provenance.onboarding.domain.usecase.provenance.tx

import io.provenance.api.models.p8e.TxResponse
import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.provenance.account.GetSigner
import io.provenance.onboarding.domain.usecase.provenance.tx.model.CreateTxOnboardAssetRequestWrapper
import io.provenance.onboarding.domain.usecase.provenance.tx.model.CreateTxRequestWrapper
import org.springframework.stereotype.Component

@Component
class CreateTxOnboardAsset(
    private val provenance: Provenance,
    private val createOnboardTx: CreateTx,
    private val getSigner: GetSigner,
) : AbstractUseCase<CreateTxOnboardAssetRequestWrapper, TxResponse>() {
    override suspend fun execute(args: CreateTxOnboardAssetRequestWrapper): TxResponse {
        val signer = getSigner.execute(args.uuid)
        val tx = createOnboardTx.execute(CreateTxRequestWrapper(args.uuid, args.request.txRequest))

        return provenance.onboard(args.request.chainId, args.request.nodeEndpoint, signer, tx)
    }
}
