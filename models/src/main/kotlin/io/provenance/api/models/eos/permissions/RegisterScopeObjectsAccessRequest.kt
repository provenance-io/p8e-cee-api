package io.provenance.api.models.eos.permissions

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig
import java.util.UUID

data class RegisterScopeObjectsAccessRequest(
    val scopeUuid: UUID,
    val type: ObjectPermissionChangeType,
    val grantee: List<String>,
    val gatewayUri: String,
    val provenanceConfig: ProvenanceConfig,
    val accountInfo: AccountInfo = AccountInfo(),
)

enum class ObjectPermissionChangeType {
    ADD,
    REMOVE
}
