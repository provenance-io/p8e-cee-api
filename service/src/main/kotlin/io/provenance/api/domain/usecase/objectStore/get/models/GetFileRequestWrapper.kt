package io.provenance.api.domain.usecase.objectStore.get.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.eos.get.GetFileRequest

data class GetFileRequestWrapper(
    val userID: UserID,
    val request: GetFileRequest,
    val useObjectStoreGateway: Boolean = false
)
