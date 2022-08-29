package io.provenance.api.domain.usecase.provenance.contracts.definitions.models

data class GetAssetDefinitionsRequest(
    val contractName: String,
    val chainId: String,
    val nodeEndpoint: String,
)
