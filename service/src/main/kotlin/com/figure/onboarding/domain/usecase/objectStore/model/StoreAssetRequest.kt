package com.figure.onboarding.domain.usecase.objectStore.model

import com.figure.onboarding.domain.usecase.common.model.AccountInfo
import com.figure.onboarding.domain.usecase.common.model.PermissionInfo
import java.util.UUID

data class StoreAssetRequest(
    val account: AccountInfo,
    val objectStoreAddress: String,
    val permissions: PermissionInfo?,
    val assetId: UUID,
    val asset: String,
)
