package io.provenance.api.models.cee

data class ContractConfig(
    val contractName: String,
    val scopeSpecificationName: String,
    val parserConfig: ParserConfig? = null,
)
