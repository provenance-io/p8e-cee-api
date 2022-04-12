package io.provenance.onboarding.fakes

import io.provenance.onboarding.domain.IdentityProvider
import java.util.UUID

class FakeIdentityProvider(private val fakeIdentity: UUID) : IdentityProvider {
    override suspend fun loggedInUser(): UUID = fakeIdentity
}
