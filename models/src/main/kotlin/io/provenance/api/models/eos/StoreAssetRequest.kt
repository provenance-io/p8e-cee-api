package io.provenance.api.models.eos

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.PermissionInfo
import java.util.UUID

data class StoreAssetRequest(
    val account: AccountInfo = AccountInfo(),
    val objectStoreAddress: String,
    val permissions: PermissionInfo?,
    val assetId: UUID,
    val asset: String,
)
