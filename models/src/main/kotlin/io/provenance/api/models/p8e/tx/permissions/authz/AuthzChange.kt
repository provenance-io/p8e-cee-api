package io.provenance.api.models.p8e.tx.permissions.authz

import java.time.OffsetDateTime

data class AuthzChange(
    val type: AuthzChangeType,
    val envelopeState: String,
    val expiration: OffsetDateTime = OffsetDateTime.now().plusHours(1),
)
