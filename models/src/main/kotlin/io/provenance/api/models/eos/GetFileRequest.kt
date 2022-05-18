package io.provenance.api.models.eos

data class GetFileRequest(
    val hash: String,
    val objectStoreAddress: String,
)
