package io.provenance.onboarding.domain.objectStore

import io.provenance.onboarding.domain.usecase.objectStore.model.StoreAssetResponse
import io.provenance.scope.encryption.proto.Encryption
import io.provenance.scope.objectstore.client.OsClient
import tech.figure.asset.v1beta1.Asset
import java.security.PrivateKey
import java.security.PublicKey

interface ObjectStore {
    fun getDIME(client: OsClient, hash: ByteArray, publicKey: PublicKey): Encryption.DIME
    fun retrieve(client: OsClient, hash: ByteArray, publicKey: PublicKey): ByteArray
    fun retrieveWithDIME(client: OsClient, hash: ByteArray, publicKey: PublicKey): Pair<Encryption.DIME, ByteArray>
    fun retrieveAndDecrypt(client: OsClient, hash: ByteArray, publicKey: PublicKey, privateKey: PrivateKey): ByteArray
    fun storeAsset(client: OsClient, asset: Asset, publicKey: PublicKey, additionalAudiences: Set<PublicKey>): StoreAssetResponse
}
