package io.provenance.api.domain.usecase.provenance.contracts.fees.models

import io.provenance.api.models.entity.EntityID

data class GetFeesForAssetRequest(
    val entityID: EntityID,
    val contractName: String,
    val assetType: String,
    val chainId: String,
    val nodeEndpoint: String,
)
