package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.scope.encryption.model.KeyRef

data class StoreObjectRequest(
    val bytes: ByteArray,
    val type: String?,
    val objectStoreUrl: String,
    val useObjectStoreGateway: Boolean,
    val keyRef: KeyRef,
    val permissions: PermissionInfo? = null,
    val account: AccountInfo = AccountInfo(),
)
