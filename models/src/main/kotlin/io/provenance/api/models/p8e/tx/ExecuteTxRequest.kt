package io.provenance.api.models.p8e.tx

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.TxBody

data class ExecuteTxRequest(
    val account: AccountInfo = AccountInfo(),
    val chainId: String,
    val nodeEndpoint: String,
    val tx: TxBody,
)
