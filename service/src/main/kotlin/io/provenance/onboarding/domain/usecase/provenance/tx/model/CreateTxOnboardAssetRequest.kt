package io.provenance.onboarding.domain.usecase.provenance.tx.model

data class CreateTxOnboardAssetRequest(
    val chainId: String,
    val nodeEndpoint: String,
    val txRequest: CreateTxRequest,
)
