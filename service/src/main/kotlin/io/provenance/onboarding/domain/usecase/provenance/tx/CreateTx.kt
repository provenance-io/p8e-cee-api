package io.provenance.onboarding.domain.usecase.provenance.tx

import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.common.model.ScopeConfig
import io.provenance.api.models.p8e.TxBody
import io.provenance.core.KeyType
import io.provenance.onboarding.domain.usecase.common.originator.GetOriginator
import io.provenance.onboarding.domain.usecase.provenance.account.GetSigner
import io.provenance.onboarding.domain.usecase.provenance.tx.model.CreateTxRequestWrapper
import io.provenance.onboarding.frameworks.config.ProvenanceProperties
import io.provenance.onboarding.frameworks.objectStore.AudienceKeyManager
import io.provenance.onboarding.frameworks.objectStore.DefaultAudience
import io.provenance.onboarding.frameworks.provenance.utility.ProvenanceUtils
import io.provenance.scope.encryption.util.getAddress
import java.lang.IllegalStateException
import org.springframework.stereotype.Component

@Component
class CreateTx(
    private val audienceKeyManager: AudienceKeyManager,
    private val getSigner: GetSigner,
    private val provenanceProperties: ProvenanceProperties,
    private val getOriginator: GetOriginator,
) : AbstractUseCase<CreateTxRequestWrapper, TxBody>() {
    override suspend fun execute(args: CreateTxRequestWrapper): TxBody {
        val utils = ProvenanceUtils()

        val account = getSigner.execute(args.uuid)
        val additionalAudiences: MutableSet<String> = mutableSetOf()

        args.request.permissions?.audiences?.forEach {
            it.uuid?.let { uuid ->
                val originator = getOriginator.execute(uuid)
                additionalAudiences.add(originator.keys[KeyType.ENCRYPTION_PUBLIC_KEY].toString())
            } ?: apply {
                it.keys?.let { kp ->
                    additionalAudiences.add(kp.encryptionKey)
                } ?: throw IllegalStateException("Audience specified does not include entity uuid or key pair.")
            }
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
