package io.provenance.api.models.p8e.tx.permissions.fees.grant

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig

data class GrantFeeGrantRequest(
    val account: AccountInfo = AccountInfo(),
    val provenanceConfig: ProvenanceConfig,
    val grant: FeeGrantDetails
)
