package io.provenance.api.domain.usecase.provenance.contracts.classify.models

import io.provenance.api.models.user.EntityID
import io.provenance.api.models.p8e.contracts.ClassifyAssetRequest

data class ClassifyAssetRequestWrapper(
    val entityID: EntityID,
    val request: ClassifyAssetRequest
)
