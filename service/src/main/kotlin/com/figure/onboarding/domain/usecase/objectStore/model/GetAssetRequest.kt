package com.figure.onboarding.domain.usecase.objectStore.model

import java.util.UUID

data class GetAssetRequest(
    val originatorUuid: UUID,
    val hash: String,
    val objectStoreAddress: String,
)
