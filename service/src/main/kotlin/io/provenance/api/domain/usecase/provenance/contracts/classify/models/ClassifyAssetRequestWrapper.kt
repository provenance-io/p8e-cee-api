package io.provenance.api.domain.usecase.provenance.contracts.classify.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.p8e.contracts.ClassifyAssetRequest

data class ClassifyAssetRequestWrapper(
    val entity: Entity,
    val request: ClassifyAssetRequest
)
