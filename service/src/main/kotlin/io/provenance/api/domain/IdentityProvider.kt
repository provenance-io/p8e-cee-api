package io.provenance.api.domain

import java.util.UUID

interface IdentityProvider {
    suspend fun loggedInUser(): UUID
}
