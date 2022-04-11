package com.figure.onboarding.domain.usecase.provenance.tx

import com.figure.onboarding.domain.provenance.Provenance
import com.figure.onboarding.domain.usecase.AbstractUseCase
import com.figure.onboarding.domain.usecase.provenance.account.GetAccount
import com.figure.onboarding.domain.usecase.provenance.tx.model.ExecuteTxRequest
import com.figure.onboarding.domain.usecase.provenance.tx.model.OnboardAssetResponse
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
