package io.provenance.api.domain.usecase.provenance.contracts.verify.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.p8e.contracts.VerifyAssetRequest

data class VerifyAssetRequestWrapper(
    val entity: Entity,
    val request: VerifyAssetRequest
)
