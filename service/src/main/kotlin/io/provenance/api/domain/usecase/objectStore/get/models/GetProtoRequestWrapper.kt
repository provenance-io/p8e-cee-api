package io.provenance.api.domain.usecase.objectStore.get.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.eos.get.GetProtoRequest

data class GetProtoRequestWrapper(
    val entity: Entity,
    val request: GetProtoRequest,
    val useObjectStoreGateway: Boolean = false,
)
