package io.provenance.api.domain.usecase.objectStore.get.models

import io.provenance.api.models.user.EntityID
import io.provenance.api.models.eos.get.GetFileRequest

data class GetFileRequestWrapper(
    val entityID: EntityID,
    val request: GetFileRequest,
    val useObjectStoreGateway: Boolean = false
)
