package io.provenance.api.domain.usecase.objectStore.replication

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.objectStore.replication.models.EnableReplicationRequest
import io.provenance.api.frameworks.config.ObjectStoreProperties
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import io.provenance.scope.util.ProtoJsonUtil.toJson
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.net.URI

@Component
class EnableReplication(
    private val objectStoreProperties: ObjectStoreProperties,
) : AbstractUseCase<EnableReplicationRequest, Unit>() {

    private val log = KotlinLogging.logger { }

    override suspend fun execute(args: EnableReplicationRequest) {
        val publicKeyResponse = OsClient(
            URI.create(args.sourceObjectStoreAddress),
            objectStoreProperties.timeoutMs,
        ).use { osClientReplicatingFrom ->
            osClientReplicatingFrom.createPublicKey(
                args.targetSigningPublicKey.toJavaPublicKey(),
                args.targetEncryptionPublicKey.toJavaPublicKey(),
                args.targetObjectStoreAddress,
            ) ?: throw IllegalStateException("Error performing operation")
        }
        log.info("createPublicKey() response: ${publicKeyResponse.toJson()}")
    }
}
