package io.provenance.api.models.cee.execute

import io.provenance.api.models.account.Participant
import io.provenance.api.models.p8e.PermissionInfo
import java.util.UUID

data class ExecuteContractRequest(
    val scopeUuid: UUID,
    val sessionUuid: UUID?,
    val config: ExecuteContractConfig,
    val records: Map<String, Any>,
    val participants: List<Participant> = emptyList(),
    val permissions: PermissionInfo?,
)
