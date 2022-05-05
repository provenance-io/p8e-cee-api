package io.provenance.cee.api.models.cee

import io.provenance.cee.api.models.account.AccountInfo

data class ExecuteContractRequest(
    val config: ExecuteContractConfig,
    val records: Map<String, Any>,
    val participants: List<AccountInfo> = emptyList()
)
