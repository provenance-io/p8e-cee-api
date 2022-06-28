package io.provenance.api.domain.usecase.provenance.tx.permission.models

import io.provenance.api.models.p8e.tx.UpdateScopeDataAccessRequest
import java.util.UUID

data class UpdateScopeDataAccessRequestWrapper(
    val uuid: UUID,
    val request: UpdateScopeDataAccessRequest,
)
