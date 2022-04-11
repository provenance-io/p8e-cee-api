package com.figure.onboarding.fakes

import com.figure.onboarding.domain.IdentityProvider
import java.util.UUID

class FakeIdentityProvider(private val fakeIdentity: UUID) : IdentityProvider {
    override suspend fun loggedInUser(): UUID = fakeIdentity
}
