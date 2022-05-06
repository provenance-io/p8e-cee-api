package io.provenance.onboarding.domain.usecase.objectStore.model

import io.provenance.api.models.account.AccountInfo
import io.provenance.onboarding.domain.usecase.common.model.PermissionInfo
import java.util.UUID

data class StoreAssetRequest(
    val account: AccountInfo,
    val objectStoreAddress: String,
    val permissions: PermissionInfo?,
    val assetId: UUID,
    val asset: String,
)
