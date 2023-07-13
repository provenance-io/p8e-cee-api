package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.user.UserID
import io.provenance.api.models.eos.permissions.RegisterScopeObjectsAccessRequest

data class RegisterScopeObjectsAccessRequestWrapper(
    val userID: UserID,
    val request: RegisterScopeObjectsAccessRequest
)
