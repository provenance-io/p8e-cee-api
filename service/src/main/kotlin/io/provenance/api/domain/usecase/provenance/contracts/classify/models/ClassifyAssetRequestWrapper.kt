package io.provenance.api.domain.usecase.provenance.contracts.classify.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.p8e.contracts.ClassifyAssetRequest

data class ClassifyAssetRequestWrapper(
    val userID: UserID,
    val request: ClassifyAssetRequest
)
