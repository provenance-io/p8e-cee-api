package io.provenance.api.models.eos

import io.provenance.api.models.p8e.PermissionInfo

data class StoreProtoRequest(
    val objectStoreAddress: String,
    val permissions: PermissionInfo?,
    val message: Any,
    val type: String,
)
