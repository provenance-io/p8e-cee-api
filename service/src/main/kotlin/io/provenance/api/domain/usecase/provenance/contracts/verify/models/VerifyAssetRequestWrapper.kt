package io.provenance.api.domain.usecase.provenance.contracts.verify.models

import io.provenance.api.models.user.EntityID
import io.provenance.api.models.p8e.contracts.VerifyAssetRequest

data class VerifyAssetRequestWrapper(
    val entityID: EntityID,
    val request: VerifyAssetRequest
)
