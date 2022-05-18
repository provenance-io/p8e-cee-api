package io.provenance.api.models.eos

data class GetProtoRequest(
    val hash: String,
    val objectStoreAddress: String,
    val type: String
)
