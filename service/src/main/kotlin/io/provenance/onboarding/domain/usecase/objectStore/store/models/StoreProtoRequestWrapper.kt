package io.provenance.onboarding.domain.usecase.objectStore.store.models

import io.provenance.api.models.eos.StoreProtoRequest
import java.util.UUID

data class StoreProtoRequestWrapper(
    val uuid: UUID,
    val request: StoreProtoRequest,
)
