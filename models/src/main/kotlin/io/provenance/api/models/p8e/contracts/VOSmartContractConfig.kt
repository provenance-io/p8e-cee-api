package io.provenance.api.models.p8e.contracts

import tech.figure.classification.asset.client.domain.model.AccessRoute
import java.util.UUID

data class VOSmartContractConfig(
    val contractAddress: String,
    val verifierAddress: String,
    val accessRoutes: List<AccessRoute> = emptyList()
)
