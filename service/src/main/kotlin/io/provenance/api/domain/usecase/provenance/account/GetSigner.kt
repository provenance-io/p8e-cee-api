package io.provenance.api.domain.usecase.provenance.account

import io.provenance.client.grpc.Signer
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.GetOriginator
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.api.frameworks.provenance.utility.ProvenanceUtils
import org.springframework.stereotype.Component
import java.security.PrivateKey
import java.security.PublicKey
import java.util.UUID

@Component
class GetSigner(
    private val getOriginator: GetOriginator,
    private val provenanceProperties: ProvenanceProperties,
) : AbstractUseCase<UUID, Signer>() {
    override suspend fun execute(args: UUID): Signer {
        val utils = ProvenanceUtils()

        val originator = getOriginator.execute(args)

        return originator.signingPublicKey().let { public ->
            originator.signingPrivateKey().let { private ->
                utils.getSigner(public as PublicKey, private as PrivateKey, provenanceProperties.mainnet)
            }
        }
    }
}
