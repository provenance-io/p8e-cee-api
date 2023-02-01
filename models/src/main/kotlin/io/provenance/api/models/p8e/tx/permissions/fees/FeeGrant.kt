package io.provenance.api.models.p8e.tx.permissions.fees

data class FeeGrant(
    val granter: String,
    val grantee: String,
    val allowance: Allowance?
)
