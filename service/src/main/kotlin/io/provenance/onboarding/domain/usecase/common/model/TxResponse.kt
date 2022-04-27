package io.provenance.onboarding.domain.usecase.common.model

data class TxResponse(
    val hash: String,
    val gasWanted: String,
    val gasUsed: String,
    val height: String,
)
