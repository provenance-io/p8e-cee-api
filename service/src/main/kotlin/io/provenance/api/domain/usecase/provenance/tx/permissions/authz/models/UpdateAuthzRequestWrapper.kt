package io.provenance.api.domain.usecase.provenance.tx.permissions.authz.models

import io.provenance.api.models.p8e.tx.permissions.authz.UpdateAuthzRequest
import java.util.UUID

data class UpdateAuthzRequestWrapper(
    val uuid: UUID,
    val request: UpdateAuthzRequest
)
