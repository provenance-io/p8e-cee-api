package io.provenance.cee.api.models.p8e

data class TxResponse(
    val hash: String,
    val gasWanted: String,
    val gasUsed: String,
    val height: String,
)
