package io.provenance.api.models.p8e

import io.provenance.api.models.account.AccountInfo

data class ExecuteTxRequest(
    val account: AccountInfo,
    val chainId: String,
    val nodeEndpoint: String,
    val tx: TxBody,
)
