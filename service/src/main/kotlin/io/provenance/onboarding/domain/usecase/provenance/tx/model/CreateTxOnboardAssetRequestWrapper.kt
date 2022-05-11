package io.provenance.onboarding.domain.usecase.provenance.tx.model

import io.provenance.api.models.p8e.CreateTxOnboardAssetRequest
import java.util.UUID

data class CreateTxOnboardAssetRequestWrapper(
    val uuid: UUID,
    val request: CreateTxOnboardAssetRequest
)
