package io.provenance.api.models.cee.execute

import io.provenance.api.models.account.Participant
import io.provenance.api.models.p8e.PermissionInfo

class ExecuteContractBatchRequest(
    val config: ExecuteContractConfig,
    val participants: List<Participant> = emptyList(),
    val permissions: PermissionInfo?,
    val chunkSize: Int = 25,
    val batch: List<BatchScopeInfo>,
)
