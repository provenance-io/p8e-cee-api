package io.provenance.onboarding.domain.usecase.provenance.tx

import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.provenance.account.GetAccount
import io.provenance.onboarding.domain.usecase.provenance.tx.model.CreateTxOnboardAssetRequest
import io.provenance.onboarding.domain.usecase.provenance.tx.model.OnboardAssetResponse
import org.springframework.stereotype.Component

@Component
class CreateTxOnboardAsset(
    private val provenance: Provenance,
    private val createOnboardTx: CreateTx,
    private val getAccount: GetAccount,
) : AbstractUseCase<CreateTxOnboardAssetRequest, OnboardAssetResponse>() {
    override suspend fun execute(args: CreateTxOnboardAssetRequest): OnboardAssetResponse {
        val account = getAccount.execute(args.txRequest.account)
        val tx = createOnboardTx.execute(args.txRequest)

        return provenance.onboard(args.chainId, args.nodeEndpoint, account, tx)
    }
}
