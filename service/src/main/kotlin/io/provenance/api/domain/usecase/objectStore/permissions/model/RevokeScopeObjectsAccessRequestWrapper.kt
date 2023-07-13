package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.eos.permissions.RevokeScopeObjectsAccessRequest
import io.provenance.api.models.user.UserID

data class RevokeScopeObjectsAccessRequestWrapper(
    val userID: UserID,
    val request: RevokeScopeObjectsAccessRequest
)
