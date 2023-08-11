package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.eos.store.StoreProtoRequest

data class StoreProtoRequestWrapper(
    val entity: Entity,
    val request: StoreProtoRequest,
    val useObjectStoreGateway: Boolean = false
)
