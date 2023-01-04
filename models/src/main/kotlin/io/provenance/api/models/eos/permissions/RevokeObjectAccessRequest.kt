package io.provenance.api.models.eos.permissions

import io.provenance.api.models.account.AccountInfo

data class RevokeObjectAccessRequest(
    val gatewayUri: String,
    val hash: String,
    val grantee: List<String>,
    val accountInfo: AccountInfo = AccountInfo(),
)
