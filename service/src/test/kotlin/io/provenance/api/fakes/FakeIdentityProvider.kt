package io.provenance.api.fakes

import io.provenance.api.domain.IdentityProvider
import java.util.UUID

class FakeIdentityProvider(private val fakeIdentity: UUID) : IdentityProvider {
    override suspend fun loggedInUser(): UUID = fakeIdentity
}
