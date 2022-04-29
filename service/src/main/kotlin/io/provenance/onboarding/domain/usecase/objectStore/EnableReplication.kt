package io.provenance.onboarding.domain.usecase.objectStore

import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.objectStore.model.EnableReplicationRequest
import io.provenance.onboarding.frameworks.config.ObjectStoreConfig
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import io.provenance.scope.util.ProtoJsonUtil.toJson
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.net.URI

@Component
class EnableReplication(
    private val objectStoreConfig: ObjectStoreConfig,
) : AbstractUseCase<EnableReplicationRequest, Unit>() {

    private val log = KotlinLogging.logger { }

    override suspend fun execute(args: EnableReplicationRequest) {
//        val osClientReplicatingFrom = OsClient(URI.create(args.sourceObjectStoreAddress), objectStoreConfig.timeoutMs)
//        val publicKeyReplicatingTo = args.targetPublicKey.toJavaPublicKey()
//        val publicKeyResponse = osClientReplicatingFrom.createPublicKey(publicKeyReplicatingTo, args.targetObjectStoreAddress)
//        log.info("createPublicKey() response: ${publicKeyResponse?.toJson()}")
//        if (publicKeyResponse == null) {
//            throw IllegalStateException("Error performing operation")
//        }
    }
}
