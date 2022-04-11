package com.figure.onboarding.domain.usecase.provenance.specifications.model

import com.figure.onboarding.domain.usecase.common.model.AccountInfo
import java.util.UUID

data class WriteSpecificationsRequest(
    val chainId: String,
    val nodeEndpoint: String,
    val account: AccountInfo,
    val scopeId: UUID,
    val contractSpecId: UUID,
    val scopeSpecId: UUID,
)
