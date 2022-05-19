package io.provenance.onboarding.domain.usecase.objectStore.get.models

import java.util.UUID

data class RetrieveAndDecryptRequest(
    val uuid: UUID,
    val objectStoreAddress: String,
    val hash: String,
)
