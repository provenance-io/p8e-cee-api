package io.provenance.api.models.eos

import java.util.UUID

data class GetAssetRequest(
    val originatorUuid: UUID,
    val hash: String,
    val objectStoreAddress: String,
)
