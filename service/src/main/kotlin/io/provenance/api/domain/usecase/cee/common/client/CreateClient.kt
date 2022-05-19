package io.provenance.api.domain.usecase.cee.common.client

import io.provenance.core.KeyType
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.api.domain.usecase.common.originator.GetOriginator
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.scope.encryption.model.DirectKeyRef
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.sdk.Affiliate
import io.provenance.scope.sdk.Client
import io.provenance.scope.sdk.ClientConfig
import io.provenance.scope.sdk.SharedClient
import org.springframework.stereotype.Component
import java.net.URI
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.util.concurrent.TimeUnit

@Component
class CreateClient(
    private val getOriginator: GetOriginator,
    private val provenanceProperties: ProvenanceProperties,
) : AbstractUseCase<CreateClientRequest, Client>() {
    override suspend fun execute(args: CreateClientRequest): Client {
        val originator = getOriginator.execute(args.uuid)
        val affiliate = Affiliate(
            signingKeyRef = DirectKeyRef(KeyPair(originator.signingPublicKey() as PublicKey, originator.signingPrivateKey() as PrivateKey)),
            encryptionKeyRef = DirectKeyRef(KeyPair(originator.encryptionPublicKey() as PublicKey, originator.encryptionPrivateKey() as PrivateKey)),
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
            args.affiliates.forEach {
                val keys = getOriginator.execute(it.uuid).keys
                client.affiliateRepository.addAffiliate(keys[KeyType.SIGNING_PUBLIC_KEY].toString().toJavaPublicKey(), keys[KeyType.ENCRYPTION_PUBLIC_KEY].toString().toJavaPublicKey())
            }
        }

        return Client(sharedClient, affiliate)
    }
}
