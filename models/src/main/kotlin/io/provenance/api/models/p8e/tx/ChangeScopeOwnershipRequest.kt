package io.provenance.api.models.p8e.tx

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.api.models.p8e.ProvenanceConfig
import java.util.UUID

data class ChangeScopeOwnershipRequest(
    val account: AccountInfo = AccountInfo(),
    val provenanceConfig: ProvenanceConfig,
    val scopeId: UUID,
    val newValueOwner: String? = null,
    val newDataAccess: PermissionInfo? = null,
)
