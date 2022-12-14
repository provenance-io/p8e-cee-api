package io.provenance.api.frameworks.objectStore

import io.provenance.api.domain.objectStore.ObjectStore
import io.provenance.api.frameworks.config.ObjectStoreProperties
import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.api.models.eos.store.toModel
import io.provenance.api.util.toModel
import io.provenance.scope.encryption.crypto.Pen
import io.provenance.scope.encryption.domain.inputstream.DIMEInputStream
import io.provenance.scope.encryption.ecies.ProvenanceKeyGenerator
import io.provenance.scope.encryption.model.DirectKeyRef
import io.provenance.scope.objectstore.client.OsClient
import java.io.ByteArrayInputStream
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.time.Duration
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import org.springframework.stereotype.Component
import tech.figure.objectstore.gateway.client.GatewayClient
import tech.figure.objectstore.gateway.client.GatewayJwt

@Component
class ObjectStoreService(
    private val osConfig: ObjectStoreProperties,
) : ObjectStore {
    // Retrieve the asset as a byte array and decrypt using the provided keypair
    override fun <T> retrieveAndDecrypt(
        client: T,
        hash: ByteArray,
        publicKey: PublicKey,
        privateKey: PrivateKey,
    ): ByteArray =
        when (client) {
            is OsClient -> {
                val future = client.get(hash, publicKey)
                val res: DIMEInputStream = future.get(osConfig.timeoutMs, TimeUnit.MILLISECONDS)
                res.getDecryptedPayload(DirectKeyRef(publicKey, privateKey)).readAllBytes()
            }
            is GatewayClient -> {
                client.getObject(
                    hash.toString(),
                    GatewayJwt.KeyPairJwt(KeyPair(publicKey, privateKey)),
                    Duration.ofMillis(osConfig.timeoutMs)
                ).`object`.objectBytes.toByteArray()
            }
            else -> throw IllegalArgumentException("Unsupported client type while retrieving object!")
        }

    override fun <T> store(client: T, message: ByteArray, publicKey: PublicKey, privateKey: PrivateKey, additionalAudiences: Set<PublicKey>, type: String?): StoreProtoResponse =
        when (client) {
            is OsClient -> {
                client.put(
                    ByteArrayInputStream(message),
                    publicKey,
                    Pen(ProvenanceKeyGenerator.generateKeyPair(publicKey)),
                    message.size.toLong(),
                    additionalAudiences,
                    type?.let { mapOf("type" to type) } ?: mapOf()
                ).get(osConfig.timeoutMs, TimeUnit.MILLISECONDS).toModel()
            }

            is GatewayClient -> {
//                client.adminPutDataStorageAccount(
//                    "tp1s2c62ke0mmwhqxguf7e2pt6e98yq38m4atwhwl",
//                    true,
//                    Duration.ofMillis(10000)
//                ).curl
                    val jwt = GatewayJwt.KeyPairJwt(KeyPair(publicKey, privateKey)).createJwt(false, OffsetDateTime.now().plusDays(1))
                println(jwt)
                client.putObject(
                    message,
                    type,
                    GatewayJwt.KeyPairJwt(KeyPair(publicKey, privateKey)),
                    Duration.ofMillis(osConfig.timeoutMs),
                    additionalAudiences.toList()
                ).toModel()
            }
            else -> throw IllegalArgumentException("Unsupported client type while storing object!")
        }
}
