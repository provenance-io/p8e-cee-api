package io.provenance.api.domain.usecase.provenance.tx.create

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.model.ScopeConfig
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.tx.create.models.CreateTxRequestWrapper
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.api.frameworks.provenance.utility.ProvenanceUtils
import io.provenance.api.models.p8e.TxBody
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

        val account = getSigner.execute(GetSignerRequest(args.uuid, args.request.account))
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
