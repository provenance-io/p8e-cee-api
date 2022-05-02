package io.provenance.onboarding.domain.usecase.cee.execute.model

import io.provenance.onboarding.domain.usecase.common.model.AccountInfo

data class ExecuteContractRequest(
    val config: ExecuteContractConfig,
    val records: Map<String, Any>,
    val participants: List<AccountInfo> = emptyList()
)
