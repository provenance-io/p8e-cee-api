package com.figure.onboarding.domain.usecase.provenance.tx

import com.figure.onboarding.domain.usecase.AbstractUseCase
import com.figure.onboarding.domain.usecase.common.model.ScopeConfig
import com.figure.onboarding.domain.usecase.common.model.TxBody
import com.figure.onboarding.domain.usecase.provenance.account.GetAccount
import com.figure.onboarding.domain.usecase.provenance.tx.model.CreateTxRequest
import com.figure.onboarding.frameworks.objectStore.AudienceKeyManager
import com.figure.onboarding.frameworks.objectStore.DefaultAudience
import com.figure.onboarding.frameworks.provenance.utility.ProvenanceUtils
import io.provenance.scope.encryption.util.getAddress
import org.springframework.stereotype.Component

@Component
class CreateTx(
    private val audienceKeyManager: AudienceKeyManager,
    private val getAccount: GetAccount
) : AbstractUseCase<CreateTxRequest, TxBody>() {
    override suspend fun execute(args: CreateTxRequest): TxBody {
        val utils = ProvenanceUtils()

        val account = getAccount.execute(args.account)
        val additionalAudiences = args.permissions?.audiences?.map { it.getAddress(!args.account.isTestNet) }?.toMutableSet() ?: mutableSetOf()

        if (args.permissions?.permissionDart == true) {
            additionalAudiences.add(audienceKeyManager.get(DefaultAudience.DART).getAddress(!args.account.isTestNet))
        }

        if (args.permissions?.permissionPortfolioManager == true) {
            additionalAudiences.add(audienceKeyManager.get(DefaultAudience.PORTFOLIO_MANAGER).getAddress(!args.account.isTestNet))
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
