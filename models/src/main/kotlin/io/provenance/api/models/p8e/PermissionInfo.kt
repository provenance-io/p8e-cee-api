package io.provenance.api.models.p8e

data class PermissionInfo(
    val audiences: Set<String> = emptySet(),
    val permissionDart: Boolean = false,
    val permissionPortfolioManager: Boolean = false,
)
