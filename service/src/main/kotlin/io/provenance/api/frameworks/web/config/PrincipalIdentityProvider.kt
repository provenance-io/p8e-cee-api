package io.provenance.api.frameworks.web.config

import io.provenance.api.domain.IdentityProvider
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PrincipalIdentityProvider : IdentityProvider {
    override suspend fun loggedInUser(): UUID = awaitSecurityContext().authentication.principal as UUID

    private suspend fun awaitSecurityContext() = ReactiveSecurityContextHolder.getContext().awaitFirst()
}
