package io.provenance.api.domain.objectStore

import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.scope.encryption.model.KeyRef
import java.io.InputStream
import java.security.PublicKey

interface ObjectStore {
    fun <T> retrieveAndDecrypt(client: T, hash: String, keyRef: KeyRef): ByteArray
    fun <T> store(client: T, message: ByteArray, keyRef: KeyRef, additionalAudiences: Set<PublicKey>, type: String?): StoreProtoResponse

    /**
     * Streams [contentLength] bytes from [message] to the object store without buffering the whole
     * payload in memory. Only supported for direct object-store clients (not the gateway); the
     * caller is responsible for closing [message]. This is the memory-safe path for large raw-byte
     * uploads where the payload is already spilled to a temporary file on disk.
     */
    fun <T> store(
        client: T,
        message: InputStream,
        contentLength: Long,
        keyRef: KeyRef,
        additionalAudiences: Set<PublicKey>,
        type: String?,
    ): StoreProtoResponse
}
