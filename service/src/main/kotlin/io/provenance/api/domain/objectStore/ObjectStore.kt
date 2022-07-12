package io.provenance.api.domain.objectStore

import com.google.protobuf.Message
import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.scope.objectstore.client.OsClient
import java.io.InputStream
import java.security.PrivateKey
import java.security.PublicKey

interface ObjectStore {
    fun retrieveAndDecrypt(client: OsClient, hash: ByteArray, publicKey: PublicKey, privateKey: PrivateKey): ByteArray
    fun storeMessage(client: OsClient, message: Message, publicKey: PublicKey, additionalAudiences: Set<PublicKey>): StoreProtoResponse
    fun storeFile(client: OsClient, file: InputStream, publicKey: PublicKey, additionalAudiences: Set<PublicKey>): StoreProtoResponse
}
