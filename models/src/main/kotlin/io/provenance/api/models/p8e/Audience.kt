package io.provenance.api.models.p8e

import java.util.UUID

data class Audience(
    val uuid: UUID?,
    var keys: AudienceKeyPair?
)

data class AudienceKeyPair(
    val encryptionKey: String,
    val signingKey: String
)
