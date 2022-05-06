package io.provenance.api.models.p8e

data class ProvenanceConfig(
    val chainId: String,
    val nodeEndpoint: String,
    val gasAdjustment: Double? = 1.5,
)
