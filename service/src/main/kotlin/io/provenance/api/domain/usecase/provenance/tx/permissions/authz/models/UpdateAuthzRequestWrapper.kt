package io.provenance.api.domain.usecase.provenance.tx.permissions.authz.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.p8e.tx.permissions.authz.UpdateAuthzRequest

data class UpdateAuthzRequestWrapper(
    val userID: UserID,
    val request: UpdateAuthzRequest
)
