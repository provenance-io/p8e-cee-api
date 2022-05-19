package io.provenance.api.models.p8e

data class PermissionInfo(
    val audiences: Set<Audience> = emptySet(),
    val permissionDart: Boolean = false,
    val permissionPortfolioManager: Boolean = false,
)
