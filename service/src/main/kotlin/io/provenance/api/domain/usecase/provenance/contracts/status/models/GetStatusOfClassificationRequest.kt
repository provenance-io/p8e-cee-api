package io.provenance.api.domain.usecase.provenance.contracts.status.models

import io.provenance.api.models.entity.Entity
import java.util.UUID

data class GetStatusOfClassificationRequest(
    val entity: Entity,
    val assetUuid: UUID,
    val assetType: String,
    val contractName: String,
    val chainId: String,
    val nodeEndpoint: String,
)
