package io.provenance.api.models.eos.get

import io.provenance.api.models.account.AccountInfo

data class GetProtoRequest(
    val hash: String,
    val objectStoreAddress: String,
    val type: String,
    val account: AccountInfo = AccountInfo()
)
