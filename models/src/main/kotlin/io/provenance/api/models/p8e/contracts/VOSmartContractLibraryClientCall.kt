package io.provenance.api.models.p8e.contracts

data class VOSmartContractLibraryClientCall(
    val methodName: String,
    val parameterClassName: String,
    val parameterClassJson: String
)
