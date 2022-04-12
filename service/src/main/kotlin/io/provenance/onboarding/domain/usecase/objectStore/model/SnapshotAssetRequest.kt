package io.provenance.onboarding.domain.usecase.objectStore.model

import io.provenance.onboarding.domain.usecase.common.model.AccountInfo
import io.provenance.onboarding.domain.usecase.common.model.PermissionInfo

data class SnapshotAssetRequest(
    val account: AccountInfo,
    val hash: String,
    val objectStoreAddress: String,
    val permissions: PermissionInfo?,
)
