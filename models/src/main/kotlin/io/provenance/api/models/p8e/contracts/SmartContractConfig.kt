package io.provenance.api.models.p8e.contracts

import tech.figure.classification.asset.client.domain.model.AccessRoute
import java.util.UUID

data class SmartContractConfig(
    val contractName: String,
    val assetUuid: UUID,
    val assetType: String = "",
    val verifierAddress: String,
    val accessRoutes: List<AccessRoute> = emptyList()
)
