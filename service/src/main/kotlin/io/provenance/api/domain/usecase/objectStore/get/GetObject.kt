package io.provenance.api.domain.usecase.objectStore.get

import io.provenance.api.domain.objectStore.ObjectStore
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.objectStore.get.models.RetrieveAndDecryptRequest
import io.provenance.api.frameworks.config.ObjectStoreProperties
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.entity.KeyType
import io.provenance.scope.objectstore.client.OsClient
import java.net.URI
import org.springframework.stereotype.Component
import tech.figure.objectstore.gateway.client.ClientConfig
import tech.figure.objectstore.gateway.client.GatewayClient

@Component
class GetObject(
    private val objectStore: ObjectStore,
    private val entityManager: EntityManager,
    private val objectStoreProperties: ObjectStoreProperties,
    private val provenanceProperties: ProvenanceProperties,
) : AbstractUseCase<RetrieveAndDecryptRequest, ByteArray>() {
    override suspend fun execute(args: RetrieveAndDecryptRequest): ByteArray {
        val entity = entityManager.getEntity(KeyManagementConfigWrapper(args.userID.toString(), args.keyManagementConfig))

        if (args.useObjectStoreGateway) {
            GatewayClient(ClientConfig(URI.create(args.objectStoreAddress), provenanceProperties.mainnet))
        } else {
            OsClient(URI.create(args.objectStoreAddress), objectStoreProperties.timeoutMs)
        }.use { client ->
            return objectStore.retrieveAndDecrypt(client, args.hash, entity.getKeyRef(KeyType.ENCRYPTION))
        }
    }
}
