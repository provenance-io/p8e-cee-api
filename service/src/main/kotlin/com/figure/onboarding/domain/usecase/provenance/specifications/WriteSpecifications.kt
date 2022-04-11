package com.figure.onboarding.domain.usecase.provenance.specifications

import com.figure.onboarding.domain.provenance.Provenance
import com.figure.onboarding.domain.usecase.AbstractUseCase
import com.figure.onboarding.domain.usecase.provenance.account.GetAccount
import com.figure.onboarding.domain.usecase.provenance.specifications.model.WriteSpecificationsRequest
import org.springframework.stereotype.Component

@Component
class WriteSpecifications(
    private val provenance: Provenance,
    private val getAccount: GetAccount
) : AbstractUseCase<WriteSpecificationsRequest, Unit>() {
    override suspend fun execute(args: WriteSpecificationsRequest) {

        val account = getAccount.execute(args.account)
        provenance.writeSpecifications(args.chainId, args.nodeEndpoint, account, args.scopeId, args.contractSpecId, args.scopeSpecId, "asset")
    }
}
