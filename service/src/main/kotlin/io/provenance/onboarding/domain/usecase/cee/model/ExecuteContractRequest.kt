package io.provenance.onboarding.domain.usecase.cee.model

data class ExecuteContractRequest(
    val config: ExecuteContractConfig,
    val records: Map<String, Any>
)
