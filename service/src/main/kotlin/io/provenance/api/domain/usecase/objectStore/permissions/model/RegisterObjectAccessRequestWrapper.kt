package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.eos.permissions.RegisterObjectAccessRequest
import io.provenance.api.models.user.UserID

data class RegisterObjectAccessRequestWrapper(
    val userID: UserID,
    val request: RegisterObjectAccessRequest
)
