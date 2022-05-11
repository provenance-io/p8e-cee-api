package io.provenance.api.models.p8e

import io.provenance.api.models.account.AccountInfo
import java.util.UUID

data class CreateTxRequest(
    val account: AccountInfo,
    val permissions: PermissionInfo?,
    val contractSpecId: UUID,
    val scopeSpecId: UUID,
    val scopeId: UUID,
    val contractInput: String,
)
