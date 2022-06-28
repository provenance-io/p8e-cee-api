package io.provenance.api.models.p8e.tx

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig
import java.util.UUID

data class UpdateScopeDataAccessRequest(
    val scopeUuid: UUID,
    val account: AccountInfo = AccountInfo(),
    val provenanceConfig: ProvenanceConfig,
    val changes: List<DataAccessUpdate>
)

data class DataAccessUpdate(
    val type: DataAccessChangeType,
    val address: String,
)

enum class DataAccessChangeType {
    ADD,
    REMOVE
}
