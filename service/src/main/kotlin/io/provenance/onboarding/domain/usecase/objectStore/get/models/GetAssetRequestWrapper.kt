package io.provenance.onboarding.domain.usecase.objectStore.get.models

import io.provenance.api.models.eos.GetProtoRequest
import java.util.UUID

data class GetAssetRequestWrapper(
    val uuid: UUID,
    val request: GetProtoRequest
)
