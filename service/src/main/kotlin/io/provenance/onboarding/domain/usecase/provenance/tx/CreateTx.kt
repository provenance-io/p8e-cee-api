package io.provenance.onboarding.domain.usecase.provenance.tx

import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.common.model.ScopeConfig
import io.provenance.onboarding.domain.usecase.common.model.TxBody
import io.provenance.onboarding.domain.usecase.provenance.account.GetAccount
import io.provenance.onboarding.domain.usecase.provenance.tx.model.CreateTxRequest
import io.provenance.onboarding.frameworks.objectStore.AudienceKeyManager
import io.provenance.onboarding.frameworks.objectStore.DefaultAudience
import io.provenance.onboarding.frameworks.provenance.utility.ProvenanceUtils
import io.provenance.scope.encryption.util.getAddress
import org.springframework.stereotype.Component

@Component
class CreateTx(
    private val audienceKeyManager: AudienceKeyManager,
    private val getAccount: GetAccount,
) : AbstractUseCase<CreateTxRequest, TxBody>() {
    override suspend fun execute(args: CreateTxRequest): TxBody {
        val utils = ProvenanceUtils()

        val account = getAccount.execute(args.account)
        val additionalAudiences: MutableSet<String> = mutableSetOf()

        args.permissions?.audiences?.forEach {
            additionalAudiences.add(it)
        }

        if (args.permissions?.permissionDart == true) {
            additionalAudiences.add(audienceKeyManager.get(DefaultAudience.DART).getAddress(!args.account.isTestNet))
        }

        if (args.permissions?.permissionPortfolioManager == true) {
            additionalAudiences.add(
                audienceKeyManager.get(DefaultAudience.PORTFOLIO_MANAGER)
                    .getAddress(!args.account.isTestNet)
            )
        }

        return utils.createScopeTx(
            ScopeConfig(
                args.scopeId,
                args.contractSpecId,
                args.scopeSpecId,
            ),
            args.contractInput,
            account.address.value,
            additionalAudiences
        )
    }
}
