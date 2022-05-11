package io.provenance.api.models.p8e

data class CreateTxOnboardAssetRequest(
    val chainId: String,
    val nodeEndpoint: String,
    val txRequest: CreateTxRequest,
)
