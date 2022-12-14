package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.eos.store.StoreProtoRequest
import java.util.UUID

data class StoreProtoRequestWrapper(
    val uuid: UUID,
    val request: StoreProtoRequest,
    val useObjectStoreGateway: Boolean = false
)
