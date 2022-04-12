package io.provenance.onboarding.domain.usecase.provenance.specifications

import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.provenance.account.GetAccount
import io.provenance.onboarding.domain.usecase.provenance.specifications.model.WriteSpecificationsRequest
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
