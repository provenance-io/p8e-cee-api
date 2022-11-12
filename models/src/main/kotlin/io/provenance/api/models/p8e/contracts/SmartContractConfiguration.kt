package io.provenance.api.models.p8e.contracts

import tech.figure.classification.asset.client.domain.model.AccessRoute

data class SmartContractConfiguration(
    val contractName: String?,
    val contractAddress: String?,
    val verifierAddress: String,
    val accessRoutes: List<AccessRoute> = emptyList()
)
