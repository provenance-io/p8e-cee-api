package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.eos.StoreAssetRequest
import java.util.UUID

data class StoreAssetRequestWrapper(
    val uuid: UUID,
    val request: StoreAssetRequest,
)
