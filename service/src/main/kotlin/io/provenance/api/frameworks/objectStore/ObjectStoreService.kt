package io.provenance.api.frameworks.objectStore

import io.provenance.api.domain.objectStore.ObjectStore
import io.provenance.api.frameworks.config.ObjectStoreProperties
import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.api.models.eos.store.toModel
import io.provenance.api.util.toModel
import io.provenance.scope.encryption.domain.inputstream.DIMEInputStream
import io.provenance.scope.encryption.model.KeyRef
import io.provenance.scope.objectstore.client.OsClient
import java.io.ByteArrayInputStream
import java.security.PublicKey
import java.time.Duration
import java.util.concurrent.TimeUnit
import org.apache.commons.codec.binary.Base64
import org.springframework.stereotype.Component
import tech.figure.objectstore.gateway.client.GatewayClient
import tech.figure.objectstore.gateway.client.GatewayJwt

@Component
class ObjectStoreService(
    private val objectStoreProperties: ObjectStoreProperties,
) : ObjectStore {
    // Retrieve the asset as a byte array and decrypt using the provided keypair
    override fun <T> retrieveAndDecrypt(
        client: T,
        hash: String,
        keyRef: KeyRef
    ): ByteArray =
        when (client) {
            is OsClient -> {
                val future = client.get(decodeBase64(hash), keyRef.publicKey)
                val res: DIMEInputStream = future.get(objectStoreProperties.timeoutMs, TimeUnit.MILLISECONDS)
                res.getDecryptedPayload(keyRef).readAllBytes()
            }
            is GatewayClient -> {
                client.getObject(
                    Base64.encodeBase64String(decodeBase64(hash)),
                    GatewayJwt.KeyRefJwt(keyRef),
                    Duration.ofMillis(objectStoreProperties.timeoutMs)
                ).`object`.objectBytes.toByteArray()
            }
            else -> throw IllegalArgumentException("Unsupported client type while retrieving object!")
        }

    override fun <T> store(client: T, message: ByteArray, keyRef: KeyRef, additionalAudiences: Set<PublicKey>, type: String?): StoreProtoResponse =
        when (client) {
            is OsClient -> {
                client.put(
                    ByteArrayInputStream(message),
                    keyRef.publicKey,
                    keyRef.signer(),
                    message.size.toLong(),
                    additionalAudiences,
                    type?.let { mapOf("type" to type) } ?: mapOf()
                ).get(objectStoreProperties.timeoutMs, TimeUnit.MILLISECONDS).toModel()
            }

            is GatewayClient -> {
                client.putObject(
                    message,
                    type,
                    GatewayJwt.KeyRefJwt(keyRef),
                    Duration.ofMillis(objectStoreProperties.timeoutMs),
                    additionalAudiences.toList()
                ).toModel()
            }
            else -> throw IllegalArgumentException("Unsupported client type while storing object!")
        }

    private fun decodeBase64(string: String): ByteArray =
        string.replace(' ', '+').let {
            Base64.decodeBase64(it)
        }
}
