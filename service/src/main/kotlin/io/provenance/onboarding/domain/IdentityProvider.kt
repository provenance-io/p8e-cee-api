package io.provenance.onboarding.domain

import java.util.UUID

interface IdentityProvider {
    suspend fun loggedInUser(): UUID
}
