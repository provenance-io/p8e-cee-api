package io.provenance.api.domain.usecase.provenance.contracts.fees.models

import io.provenance.api.models.user.UserID

data class GetFeesForAssetRequest(
    val userID: UserID,
    val contractName: String,
    val assetType: String,
    val chainId: String,
    val nodeEndpoint: String,
)
