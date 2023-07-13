package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.user.UserID
import io.provenance.api.models.eos.permissions.RevokeObjectAccessRequest

data class RevokeObjectAccessRequestWrapper(
    val userID: UserID,
    val request: RevokeObjectAccessRequest
)
