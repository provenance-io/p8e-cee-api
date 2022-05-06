package io.provenance.onboarding.domain.usecase.provenance.tx.model

import io.provenance.api.models.account.AccountInfo
import io.provenance.onboarding.domain.usecase.common.model.PermissionInfo
import java.util.UUID

data class CreateTxRequest(
    val account: AccountInfo,
    val permissions: PermissionInfo?,
    val contractSpecId: UUID,
    val scopeSpecId: UUID,
    val scopeId: UUID,
    val contractInput: String,
)
