package io.provenance.api.domain.objectStore

import io.provenance.api.models.eos.store.StoreProtoResponse
import java.security.PrivateKey
import java.security.PublicKey

interface ObjectStore {
    fun <T> retrieveAndDecrypt(client: T, hash: ByteArray, publicKey: PublicKey, privateKey: PrivateKey): ByteArray
    fun <T> store(client: T, message: ByteArray, publicKey: PublicKey, privateKey: PrivateKey, additionalAudiences: Set<PublicKey>, type: String?): StoreProtoResponse
}
