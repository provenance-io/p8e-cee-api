package io.provenance.onboarding.domain.usecase.provenance.tx

import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.provenance.account.GetAccount
import io.provenance.onboarding.domain.usecase.provenance.tx.model.ExecuteTxRequest
import io.provenance.onboarding.domain.usecase.provenance.tx.model.OnboardAssetResponse
import org.springframework.stereotype.Component

@Component
class ExecuteTx(
    private val provenance: Provenance,
    private val getAccount: GetAccount,
) : AbstractUseCase<ExecuteTxRequest, OnboardAssetResponse>() {
    override suspend fun execute(args: ExecuteTxRequest): OnboardAssetResponse {
        val account = getAccount.execute(args.account)
        return provenance.onboard(args.chainId, args.nodeEndpoint, account, args.tx)
    }
}
