package com.figure.onboarding.domain.usecase.provenance.tx.model

import com.figure.onboarding.domain.usecase.common.model.AccountInfo
import com.figure.onboarding.domain.usecase.common.model.PermissionInfo
import java.util.UUID

data class CreateTxRequest(
    val account: AccountInfo,
    val permissions: PermissionInfo?,
    val contractSpecId: UUID,
    val scopeSpecId: UUID,
    val scopeId: UUID,
    val contractInput: String,
)
