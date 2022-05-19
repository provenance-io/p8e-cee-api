package io.provenance.onboarding.domain.usecase.provenance.tx

import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.common.model.ScopeConfig
import io.provenance.api.models.p8e.TxBody
import io.provenance.onboarding.domain.usecase.common.originator.EntityManager
import io.provenance.onboarding.domain.usecase.provenance.account.GetSigner
import io.provenance.onboarding.domain.usecase.provenance.tx.model.CreateTxRequestWrapper
import io.provenance.onboarding.frameworks.config.ProvenanceProperties
import io.provenance.onboarding.frameworks.provenance.utility.ProvenanceUtils
import io.provenance.scope.encryption.util.getAddress
import io.provenance.scope.encryption.util.toJavaPublicKey
import org.springframework.stereotype.Component

@Component
class CreateTx(
    private val entityManager: EntityManager,
    private val getSigner: GetSigner,
    private val provenanceProperties: ProvenanceProperties,
) : AbstractUseCase<CreateTxRequestWrapper, TxBody>() {
    override suspend fun execute(args: CreateTxRequestWrapper): TxBody {
        val utils = ProvenanceUtils()

        val account = getSigner.execute(args.uuid)
        val additionalAudiences = entityManager.hydrateKeys(args.request.permissions)

        return utils.createScopeTx(
            ScopeConfig(
                args.request.scopeId,
                args.request.contractSpecId,
                args.request.scopeSpecId,
            ),
            args.request.contractInput,
            account.address(),
            additionalAudiences.map { it.signingKey.toJavaPublicKey().getAddress(provenanceProperties.mainnet) }.toSet()
        )
    }
}
