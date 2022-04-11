package com.figure.onboarding.domain.usecase.provenance.tx.model

import com.figure.onboarding.domain.usecase.common.model.AccountInfo
import com.figure.onboarding.domain.usecase.common.model.TxBody

data class ExecuteTxRequest(
    val account: AccountInfo,
    val chainId: String,
    val nodeEndpoint: String,
    val tx: TxBody,
)
