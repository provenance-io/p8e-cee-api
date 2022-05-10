package io.provenance.onboarding.domain.usecase.common.model

data class PermissionInfo(
    val audiences: Set<String> = emptySet(),
    val permissionDart: Boolean = false,
    val permissionPortfolioManager: Boolean = false,
)
