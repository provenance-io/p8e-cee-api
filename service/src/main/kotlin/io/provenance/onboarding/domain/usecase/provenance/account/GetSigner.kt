package io.provenance.onboarding.domain.usecase.provenance.account

import io.provenance.client.grpc.Signer
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.common.originator.EntityManager
import io.provenance.onboarding.frameworks.config.ProvenanceProperties
import io.provenance.onboarding.frameworks.provenance.utility.ProvenanceUtils
import org.springframework.stereotype.Component
import java.security.PrivateKey
import java.security.PublicKey
import java.util.UUID

@Component
class GetSigner(
    private val entityManager: EntityManager,
    private val provenanceProperties: ProvenanceProperties,
) : AbstractUseCase<UUID, Signer>() {
    override suspend fun execute(args: UUID): Signer {
        val utils = ProvenanceUtils()

        val originator = entityManager.getEntity(args)

        return originator.signingPublicKey().let { public ->
            originator.signingPrivateKey().let { private ->
                utils.getSigner(public as PublicKey, private as PrivateKey, provenanceProperties.mainnet)
            }
        }
    }
}
