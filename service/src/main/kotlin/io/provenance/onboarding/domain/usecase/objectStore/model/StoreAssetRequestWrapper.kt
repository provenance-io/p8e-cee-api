package io.provenance.onboarding.domain.usecase.objectStore.model

import java.util.UUID

data class StoreAssetRequestWrapper(
    val uuid: UUID,
    val request: StoreAssetRequest,
)
