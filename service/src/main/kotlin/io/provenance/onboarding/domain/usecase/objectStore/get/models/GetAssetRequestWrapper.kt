package io.provenance.onboarding.domain.usecase.objectStore.get.models

import io.provenance.api.models.eos.GetAssetRequest
import java.util.UUID

data class GetAssetRequestWrapper(
    val uuid: UUID,
    val request: GetAssetRequest
)
