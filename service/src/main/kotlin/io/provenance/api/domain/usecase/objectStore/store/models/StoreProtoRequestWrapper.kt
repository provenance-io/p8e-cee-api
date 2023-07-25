package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.user.EntityID
import io.provenance.api.models.eos.store.StoreProtoRequest

data class StoreProtoRequestWrapper(
    val entityID: EntityID,
    val request: StoreProtoRequest,
    val useObjectStoreGateway: Boolean = false
)
