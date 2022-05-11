package io.provenance.onboarding.domain.usecase.objectStore.model

import java.util.UUID

data class SnapshotAssetRequestWrapper(
    val uuid: UUID,
    val request: SnapshotAssetRequest
)
