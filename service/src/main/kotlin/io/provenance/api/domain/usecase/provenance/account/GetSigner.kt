package io.provenance.api.domain.usecase.provenance.account

import io.provenance.client.grpc.Signer
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.api.frameworks.provenance.utility.ProvenanceUtils
import org.springframework.stereotype.Component
import java.security.PrivateKey
import java.security.PublicKey

@Component
class GetSigner(
    private val entityManager: EntityManager,
    private val provenanceProperties: ProvenanceProperties,
) : AbstractUseCase<GetSignerRequest, Signer>() {
    override suspend fun execute(args: GetSignerRequest): Signer {
        val utils = ProvenanceUtils()

        val originator = entityManager.getEntity(KeyManagementConfigWrapper(args.uuid, args.account.keyManagementConfig))

        return originator.signingPublicKey().let { public ->
            originator.signingPrivateKey().let { private ->
                utils.getSigner(public as PublicKey, private as PrivateKey, provenanceProperties.mainnet)
            }
        }
    }
}
