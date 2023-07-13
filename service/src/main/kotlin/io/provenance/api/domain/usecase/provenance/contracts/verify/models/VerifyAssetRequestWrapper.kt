package io.provenance.api.domain.usecase.provenance.contracts.verify.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.p8e.contracts.VerifyAssetRequest

data class VerifyAssetRequestWrapper(
    val userID: UserID,
    val request: VerifyAssetRequest
)
