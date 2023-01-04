package io.provenance.api.models.eos.permissions

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig
import java.util.UUID

data class RevokeScopeObjectsAccessRequest(
    val scopeUuid: UUID,
    val grantee: List<String>,
    val gatewayUri: String,
    val provenanceConfig: ProvenanceConfig,
    val accountInfo: AccountInfo = AccountInfo(),
)
