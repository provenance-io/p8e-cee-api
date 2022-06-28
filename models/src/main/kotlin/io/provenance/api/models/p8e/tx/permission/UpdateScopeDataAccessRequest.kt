package io.provenance.api.models.p8e.tx.permission

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig
import java.util.UUID

data class UpdateScopeDataAccessRequest(
    val scopeUuid: UUID,
    val account: AccountInfo = AccountInfo(),
    val provenanceConfig: ProvenanceConfig,
    val changes: List<DataAccessUpdate>
)



