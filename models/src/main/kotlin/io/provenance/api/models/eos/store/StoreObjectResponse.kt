package io.provenance.api.models.eos.store

data class StoreObjectResponse(
    val hash: String
)

fun StoreProtoResponse.toModel() = StoreObjectResponse(hash)
