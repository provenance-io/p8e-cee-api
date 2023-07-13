package io.provenance.api.domain.usecase.provenance.account

import cosmos.crypto.secp256k1.Keys
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.client.grpc.Signer
import io.provenance.entity.KeyType
import io.provenance.scope.util.toByteString
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.springframework.stereotype.Component

@Component
class GetSigner(
    private val entityManager: EntityManager,
    private val provenanceProperties: ProvenanceProperties,
) : AbstractUseCase<GetSignerRequest, Signer>() {
    override suspend fun execute(args: GetSignerRequest): Signer =
        entityManager.getEntity(KeyManagementConfigWrapper(args.entity, args.account.keyManagementConfig)).let { entity ->
            object : Signer {
                override fun address(): String = entity.address(KeyType.SIGNING, provenanceProperties.mainnet)

                override fun pubKey(): Keys.PubKey =
                    Keys.PubKey.newBuilder()
                        .setKey((entity.publicKey(KeyType.SIGNING) as BCECPublicKey).q.getEncoded(true).toByteString())
                        .build()

                override fun sign(data: ByteArray): ByteArray = entity.sign(KeyType.SIGNING, data)
            }
        }
}
