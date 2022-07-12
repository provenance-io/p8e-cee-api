package io.provenance.api.domain.objectStore

import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.scope.objectstore.client.OsClient
import java.security.PrivateKey
import java.security.PublicKey

interface ObjectStore {
    fun retrieveAndDecrypt(client: OsClient, hash: ByteArray, publicKey: PublicKey, privateKey: PrivateKey): ByteArray
    fun <T> store(client: OsClient, message: T, publicKey: PublicKey, additionalAudiences: Set<PublicKey>): StoreProtoResponse
}
