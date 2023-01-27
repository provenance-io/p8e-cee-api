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
        entityManager.getEntity(KeyManagementConfigWrapper(args.uuid.toString(), args.account.keyManagementConfig)).let {
            object : Signer {
                override fun address(): String = it.address(KeyType.SIGNING, provenanceProperties.mainnet)

                override fun pubKey(): Keys.PubKey =
                    Keys.PubKey.newBuilder()
                        .setKey((it.publicKey(KeyType.SIGNING) as BCECPublicKey).q.getEncoded(true).toByteString())
                        .build()

                override fun sign(data: ByteArray): ByteArray = it.sign(KeyType.SIGNING, data)
            }
        }
}
