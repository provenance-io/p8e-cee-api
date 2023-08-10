package io.provenance.api.domain.usecase.objectStore.get.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.eos.get.GetFileRequest

data class GetFileRequestWrapper(
    val entity: Entity,
    val request: GetFileRequest,
    val useObjectStoreGateway: Boolean = false
)
