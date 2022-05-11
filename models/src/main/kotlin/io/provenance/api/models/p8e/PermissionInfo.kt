package io.provenance.api.models.p8e

import java.security.PublicKey

data class PermissionInfo(
    val audiences: Set<PublicKey> = emptySet(),
    val permissionDart: Boolean = false,
    val permissionPortfolioManager: Boolean = false,
)
