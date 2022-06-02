package io.provenance.api.models.eos.get

import io.provenance.api.models.account.AccountInfo

data class GetFileRequest(
    val hash: String,
    val objectStoreAddress: String,
    val accountInfo: AccountInfo = AccountInfo()
)
