package io.provenance.api.models.p8e.tx.permissions.fees.grant

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.tx.permissions.fees.Allowance

data class GrantFeeGrantRequest(
    val account: AccountInfo = AccountInfo(),
    val provenanceConfig: ProvenanceConfig,
    val grantee: String,
    val grant: FeeGrantDetails
)

data class FeeGrantDetails(
    val denom: String,
    val allowance: Allowance
)
