package io.provenance.api.domain.usecase.objectStore.get.models

import io.provenance.api.models.entity.EntityID
import io.provenance.api.models.eos.get.GetProtoRequest

data class GetProtoRequestWrapper(
    val entityID: EntityID,
    val request: GetProtoRequest,
    val useObjectStoreGateway: Boolean = false,
)
