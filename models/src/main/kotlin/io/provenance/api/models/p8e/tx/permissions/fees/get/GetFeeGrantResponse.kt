package io.provenance.api.models.p8e.tx.permissions.fees.get

import io.provenance.api.models.p8e.tx.permissions.fees.Allowance

data class GetFeeGrantResponse(
    val granter: String,
    val grantee: String,
    val allowance: Allowance
)
