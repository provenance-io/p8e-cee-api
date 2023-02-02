package io.provenance.api.models.p8e.tx.permissions.fees.get

data class GetFeeGrantAllowanceRequest(
    val nodeEndpoint: String,
    val chainId: String,
    val granter: String,
    val grantee: String
)
