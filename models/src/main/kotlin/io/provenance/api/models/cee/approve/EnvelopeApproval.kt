package io.provenance.api.models.cee.approve

import java.time.OffsetDateTime

data class EnvelopeApproval(
    val envelope: ByteArray,
    val expiration: OffsetDateTime = OffsetDateTime.now().plusHours(1),
)
