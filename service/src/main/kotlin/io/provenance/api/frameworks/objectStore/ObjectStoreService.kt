package io.provenance.api.frameworks.objectStore

import com.google.protobuf.Message
import io.provenance.api.domain.objectStore.ObjectStore
import io.provenance.api.frameworks.config.ObjectStoreConfig
import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.api.models.eos.store.toModel
import io.provenance.scope.encryption.crypto.Pen
import io.provenance.scope.encryption.domain.inputstream.DIMEInputStream
import io.provenance.scope.encryption.ecies.ProvenanceKeyGenerator
import io.provenance.scope.encryption.model.DirectKeyRef
import io.provenance.scope.objectstore.client.OsClient
import java.io.InputStream
import java.security.PrivateKey
import java.security.PublicKey
import java.util.concurrent.TimeUnit
import org.springframework.stereotype.Component

@Component
class ObjectStoreService(
    private val osConfig: ObjectStoreConfig
) : ObjectStore {
    // Retrieve the asset as a byte array and decrypt using the provided keypair
    override fun retrieveAndDecrypt(
        client: OsClient,
        hash: ByteArray,
        publicKey: PublicKey,
        privateKey: PrivateKey,
    ): ByteArray {
        val future = client.get(hash, publicKey)
        val res: DIMEInputStream = future.get(osConfig.timeoutMs, TimeUnit.MILLISECONDS)
        return res.getDecryptedPayload(DirectKeyRef(publicKey, privateKey)).readAllBytes()
    }

    override fun storeMessage(
        client: OsClient,
        message: Message,
        publicKey: PublicKey,
        additionalAudiences: Set<PublicKey>
    ): StoreProtoResponse {
        val future = client.put(
            message,
            publicKey,
            Pen(ProvenanceKeyGenerator.generateKeyPair(publicKey)),
            additionalAudiences
        )

        return future.get(osConfig.timeoutMs, TimeUnit.MILLISECONDS).toModel()
    }

    override fun storeFile(client: OsClient, file: InputStream, publicKey: PublicKey, additionalAudiences: Set<PublicKey>): StoreProtoResponse {
        val future = client.put(
            file,
            publicKey,
            Pen(ProvenanceKeyGenerator.generateKeyPair(publicKey)),
            file.available().toLong(),
            additionalAudiences
        )

        return future.get(osConfig.timeoutMs, TimeUnit.MILLISECONDS).toModel()
    }
}
