package com.figure.onboarding.domain.usecase.common.model

import java.security.PublicKey

data class PermissionInfo(
    val audiences: Set<PublicKey> = emptySet(),
    val permissionDart: Boolean = false,
    val permissionPortfolioManager: Boolean = false,
)
