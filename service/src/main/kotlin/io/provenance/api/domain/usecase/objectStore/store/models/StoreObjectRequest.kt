package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.PermissionInfo
import java.security.PrivateKey
import java.security.PublicKey

data class StoreObjectRequest(
    val bytes: ByteArray,
    val type: String?,
    val objectStoreUrl: String,
    val useObjectStoreGateway: Boolean,
    val publicKey: PublicKey,
    val privateKey: PrivateKey,
    val permissions: PermissionInfo? = null,
    val account: AccountInfo = AccountInfo(),
)
