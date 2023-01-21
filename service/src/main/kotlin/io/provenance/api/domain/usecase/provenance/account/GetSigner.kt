package io.provenance.api.domain.usecase.provenance.account

import cosmos.crypto.secp256k1.Keys
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.client.grpc.Signer
import io.provenance.hdwallet.common.hashing.sha256
import io.provenance.hdwallet.ec.extensions.toECPrivateKey
import io.provenance.hdwallet.signer.BCECSigner
import io.provenance.scope.encryption.util.getAddress
import io.provenance.scope.util.toByteString
import java.security.PrivateKey
import java.security.PublicKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.springframework.stereotype.Component

@Component
class GetSigner(
    private val entityManager: EntityManager,
    private val provenanceProperties: ProvenanceProperties,
) : AbstractUseCase<GetSignerRequest, Signer>() {
    override suspend fun execute(args: GetSignerRequest): Signer {
        val originator = entityManager.getEntity(KeyManagementConfigWrapper(args.uuid.toString(), args.account.keyManagementConfig))

        return (originator.signingPublicKey() as PublicKey).let { public ->
            (originator.signingPrivateKey() as PrivateKey).let { private ->
                object : Signer {
                    override fun address(): String = public.getAddress(provenanceProperties.mainnet)

                    override fun pubKey(): Keys.PubKey =
                        Keys.PubKey.newBuilder()
                            .setKey((public as BCECPublicKey).q.getEncoded(true).toByteString())
                            .build()

                    override fun sign(data: ByteArray): ByteArray =
                        BCECSigner().sign(private.toECPrivateKey(), data.sha256())
                            .encodeAsBTC()
                            .toByteArray()
                }
            }
        }
    }
}
