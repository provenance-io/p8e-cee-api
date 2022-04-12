package io.provenance.onboarding.domain.usecase.provenance.tx.model

data class OnboardAssetResponse(
    val hash: String,
    val gasWanted: String,
    val gasUsed: String,
    val height: String,
)
