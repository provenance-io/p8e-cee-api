package io.provenance.api.domain.objectStore

import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.scope.encryption.model.KeyRef
import java.security.PublicKey

interface ObjectStore {
    fun <T> retrieveAndDecrypt(client: T, hash: String, keyRef: KeyRef): ByteArray
    fun <T> store(client: T, message: ByteArray, keyRef: KeyRef, additionalAudiences: Set<PublicKey>, type: String?): StoreProtoResponse
}
