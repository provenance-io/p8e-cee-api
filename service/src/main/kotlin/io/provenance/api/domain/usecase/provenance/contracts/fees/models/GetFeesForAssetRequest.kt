package io.provenance.api.domain.usecase.provenance.contracts.fees.models

import java.util.UUID

data class GetFeesForAssetRequest(
    val uuid: UUID,
    val contractName: String,
    val assetType: String,
    val chainId: String,
    val nodeEndpoint: String,
)
