package io.provenance.api.models.eos

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.PermissionInfo
import java.util.UUID

data class SnapshotAssetRequest(
    val uuid: UUID,
    val account: AccountInfo = AccountInfo(),
    val hash: String,
    val objectStoreAddress: String,
    val permissions: PermissionInfo?,
)
