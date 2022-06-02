package io.provenance.api.domain.usecase.provenance.contracts.verify.models

import io.provenance.api.models.p8e.contracts.VerifyAssetRequest
import java.util.UUID

data class VerifyAssetRequestWrapper(
    val uuid: UUID,
    val request: VerifyAssetRequest
)
