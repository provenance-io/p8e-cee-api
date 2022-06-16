package io.provenance.api.models.cee.execute

import io.provenance.api.models.account.Participant
import io.provenance.api.models.p8e.PermissionInfo

data class ExecuteContractBatchRequest(
    val config: ExecuteContractConfig,
    val records: Map<String, Any>,
    val participants: List<Participant> = emptyList(),
    val permissions: PermissionInfo?,
    val chunkSize: Int = 25,
    val batch: List<ScopeInfo> = emptyList(),
)
