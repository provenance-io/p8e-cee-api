package io.provenance.api.models.p8e.tx.permissions.fees.revoke

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig

data class RevokeFeeGrantRequest(
    val provenanceConfig: ProvenanceConfig,
    val account: AccountInfo = AccountInfo(),
    val grantee: String,
)
