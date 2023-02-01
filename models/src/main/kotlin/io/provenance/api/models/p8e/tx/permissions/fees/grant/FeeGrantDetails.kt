package io.provenance.api.models.p8e.tx.permissions.fees.grant

import io.provenance.api.models.p8e.tx.permissions.fees.Allowance

data class FeeGrantDetails(
    val grantee: String,
    val allowance: Allowance
)
