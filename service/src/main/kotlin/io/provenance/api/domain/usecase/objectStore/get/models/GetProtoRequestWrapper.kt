package io.provenance.api.domain.usecase.objectStore.get.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.eos.get.GetProtoRequest

data class GetProtoRequestWrapper(
    val userID: UserID,
    val request: GetProtoRequest,
    val useObjectStoreGateway: Boolean = false,
)
