package io.provenance.api.domain.usecase.provenance.contracts.classify.models

import io.provenance.api.models.p8e.contracts.ClassifyAssetRequest
import java.util.UUID

data class ClassifyAssetRequestWrapper(
    val uuid: UUID,
    val request: ClassifyAssetRequest
)
