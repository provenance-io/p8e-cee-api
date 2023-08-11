package io.provenance.api.domain.usecase.provenance.contracts.fees.models

import io.provenance.api.models.entity.Entity

data class GetFeesForAssetRequest(
    val entity: Entity,
    val contractName: String,
    val assetType: String,
    val chainId: String,
    val nodeEndpoint: String,
)
