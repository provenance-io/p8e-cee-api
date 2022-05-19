package io.provenance.api.domain.usecase.provenance.tx

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.model.ScopeConfig
import io.provenance.api.models.p8e.TxBody
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.tx.model.CreateTxRequestWrapper
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.api.frameworks.objectStore.AudienceKeyManager
import io.provenance.api.frameworks.objectStore.DefaultAudience
import io.provenance.api.frameworks.provenance.utility.ProvenanceUtils
import io.provenance.scope.encryption.util.getAddress
import org.springframework.stereotype.Component

@Component
class CreateTx(
    private val audienceKeyManager: AudienceKeyManager,
    private val getSigner: GetSigner,
    private val provenanceProperties: ProvenanceProperties,
) : AbstractUseCase<CreateTxRequestWrapper, TxBody>() {
    override suspend fun execute(args: CreateTxRequestWrapper): TxBody {
        val utils = ProvenanceUtils()

        val account = getSigner.execute(args.uuid)
        val additionalAudiences: MutableSet<String> = mutableSetOf()

        args.request.permissions?.audiences?.forEach {
            additionalAudiences.add(it)
        }

        if (args.request.permissions?.permissionDart == true) {
            additionalAudiences.add(audienceKeyManager.get(DefaultAudience.DART).getAddress(provenanceProperties.mainnet))
        }

        if (args.request.permissions?.permissionPortfolioManager == true) {
            additionalAudiences.add(audienceKeyManager.get(DefaultAudience.PORTFOLIO_MANAGER).getAddress(provenanceProperties.mainnet))
        }

        return utils.createScopeTx(
            ScopeConfig(
                args.request.scopeId,
                args.request.contractSpecId,
                args.request.scopeSpecId,
            ),
            args.request.contractInput,
            account.address(),
            additionalAudiences
        )
    }
}
