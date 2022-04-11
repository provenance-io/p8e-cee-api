package com.figure.onboarding.domain.usecase.objectStore.model

import com.figure.onboarding.domain.usecase.common.model.AccountInfo
import com.figure.onboarding.domain.usecase.common.model.PermissionInfo

data class SnapshotAssetRequest(
    val account: AccountInfo,
    val hash: String,
    val objectStoreAddress: String,
    val permissions: PermissionInfo?,
)
