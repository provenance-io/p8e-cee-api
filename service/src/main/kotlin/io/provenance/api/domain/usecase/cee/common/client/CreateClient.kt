package io.provenance.api.domain.usecase.cee.common.client

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.entity.KeyType
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.sdk.Affiliate
import io.provenance.scope.sdk.Client
import io.provenance.scope.sdk.ClientConfig
import io.provenance.scope.sdk.SharedClient
import java.net.URI
import java.util.concurrent.TimeUnit
import org.springframework.stereotype.Component

@Component
class CreateClient(
    private val provenanceProperties: ProvenanceProperties,
    private val entityManager: EntityManager,
) : AbstractUseCase<CreateClientRequest, Client>() {
    override suspend fun execute(args: CreateClientRequest): Client {
        val originator = entityManager.getEntity(
            KeyManagementConfigWrapper(
                entityId = args.entity.id,
                config = args.account.keyManagementConfig
            )
        )
        val affiliate = Affiliate(
            signingKeyRef = originator.getKeyRef(KeyType.SIGNING),
            encryptionKeyRef = originator.getKeyRef(KeyType.ENCRYPTION),
            args.account.partyType,
        )

        val sharedClient = SharedClient(
            ClientConfig(
                mainNet = provenanceProperties.mainnet,
                cacheJarSizeInBytes = 4L * 1024 * 1024, // ~ 4 MB,
                cacheRecordSizeInBytes = 0L,
                cacheSpecSizeInBytes = 0L,
                disableContractLogs = provenanceProperties.mainnet,
                osConcurrencySize = 6,
                osGrpcUrl = URI(args.client.objectStoreUrl),
                osChannelCustomizeFn = { channelBuilder ->
                    channelBuilder
                        .idleTimeout(1, TimeUnit.MINUTES)
                        .keepAliveTime(10, TimeUnit.SECONDS)
                        .keepAliveTimeout(10, TimeUnit.SECONDS)
                }
            )
        ).also { client ->
            args.affiliates.forEach { kp ->
                client.affiliateRepository.addAffiliate(kp.signingKey.toJavaPublicKey(), kp.encryptionKey.toJavaPublicKey())
            }
        }

        return Client(sharedClient, affiliate)
    }
}
