package io.provenance.api.models.p8e.tx.permissions.authz

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.ObjectStoreConfig
import io.provenance.api.models.p8e.ProvenanceConfig

data class UpdateAuthzRequest(
    val client: ObjectStoreConfig,
    val account: AccountInfo = AccountInfo(),
    val provenanceConfig: ProvenanceConfig,
    val changes: List<AuthzChange>
)
