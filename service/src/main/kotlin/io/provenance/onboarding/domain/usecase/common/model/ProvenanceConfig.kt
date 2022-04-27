package io.provenance.onboarding.domain.usecase.common.model

data class ProvenanceConfig(
    val chainId: String,
    val nodeEndpoint: String,
    val gasAdjustment: Double? = 1.5,
)
