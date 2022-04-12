package io.provenance.onboarding.domain.usecase.provenance.tx.model

import io.provenance.onboarding.domain.usecase.common.model.AccountInfo
import io.provenance.onboarding.domain.usecase.common.model.TxBody

data class ExecuteTxRequest(
    val account: AccountInfo,
    val chainId: String,
    val nodeEndpoint: String,
    val tx: TxBody,
)
