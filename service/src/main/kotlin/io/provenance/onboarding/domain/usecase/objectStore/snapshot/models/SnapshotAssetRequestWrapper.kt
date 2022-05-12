package io.provenance.onboarding.domain.usecase.objectStore.snapshot.models

import io.provenance.api.models.eos.SnapshotAssetRequest
import java.util.UUID

data class SnapshotAssetRequestWrapper(
    val uuid: UUID,
    val request: SnapshotAssetRequest
)
