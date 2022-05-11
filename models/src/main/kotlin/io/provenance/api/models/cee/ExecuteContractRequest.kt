package io.provenance.api.models.cee

import io.provenance.api.models.account.Participant

data class ExecuteContractRequest(
    val config: ExecuteContractConfig,
    val records: Map<String, Any>,
    val participants: List<Participant> = emptyList()
)
