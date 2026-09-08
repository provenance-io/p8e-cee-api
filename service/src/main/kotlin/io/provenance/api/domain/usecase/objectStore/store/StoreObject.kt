package io.provenance.api.domain.usecase.objectStore.store

import io.provenance.api.domain.objectStore.ObjectStore
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.objectStore.store.models.StoreObjectRequest
import io.provenance.api.domain.usecase.objectStore.store.models.StoreObjectStreamRequest
import io.provenance.api.frameworks.config.ObjectStoreProperties
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import java.net.URI
import kotlin.io.path.inputStream
import org.springframework.stereotype.Component
import tech.figure.objectstore.gateway.client.ClientConfig
import tech.figure.objectstore.gateway.client.GatewayClient

@Component
class StoreObject(
    private val objectStore: ObjectStore,
    private val objectStoreProperties: ObjectStoreProperties,
    private val provenanceProperties: ProvenanceProperties,
    private val entityManager: EntityManager,
) : AbstractUseCase<StoreObjectRequest, StoreProtoResponse>() {
    override suspend fun execute(args: StoreObjectRequest): StoreProtoResponse {
        val additionalAudiences = entityManager.hydrateKeys(args.permissions)

        val client = if (args.useObjectStoreGateway)
            GatewayClient(ClientConfig(URI.create(args.objectStoreUrl), provenanceProperties.mainnet))
        else
            OsClient(URI.create(args.objectStoreUrl), objectStoreProperties.timeoutMs)

        client.use {
            return objectStore.store(
                client,
                args.bytes,
                args.keyRef,
                additionalAudiences.map { it.encryptionKey.toJavaPublicKey() }.toSet(),
                args.type
            )
        }
    }

    /*
     * Streaming counterpart to execute(StoreObjectRequest). Uploads directly from the temp file on
     * disk via an InputStream so a large raw payload never has to be held in memory as a ByteArray.
     * Only ever uses the direct OsClient -- the gateway client cannot stream -- so callers must
     * restrict this path to non-gateway, raw-byte stores.
     */
    suspend fun executeStreaming(args: StoreObjectStreamRequest): StoreProtoResponse {
        val additionalAudiences = entityManager.hydrateKeys(args.permissions)

        OsClient(URI.create(args.objectStoreUrl), objectStoreProperties.timeoutMs).use { client ->
            args.source.inputStream().use { stream ->
                return objectStore.store(
                    client,
                    stream,
                    args.contentLength,
                    args.keyRef,
                    additionalAudiences.map { it.encryptionKey.toJavaPublicKey() }.toSet(),
                    args.type
                )
            }
        }
    }
}
