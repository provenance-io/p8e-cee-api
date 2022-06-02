package io.provenance.api.models.p8e.tx

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.PermissionInfo
import java.util.UUID

data class CreateTxRequest(
    val account: AccountInfo = AccountInfo(),
    val permissions: PermissionInfo?,
    val contractSpecId: UUID,
    val scopeSpecId: UUID,
    val scopeId: UUID,
    val contractInput: String,
)
