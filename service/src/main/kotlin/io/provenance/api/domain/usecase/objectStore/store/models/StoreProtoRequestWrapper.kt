package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.eos.store.StoreProtoRequest

data class StoreProtoRequestWrapper(
    val userID: UserID,
    val request: StoreProtoRequest,
    val useObjectStoreGateway: Boolean = false
)
