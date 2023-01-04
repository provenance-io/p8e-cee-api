package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.eos.permissions.RevokeScopeObjectsAccessRequest
import java.util.UUID

data class RevokeScopeObjectsAccessRequestWrapper(
    val uuid: UUID,
    val request: RevokeScopeObjectsAccessRequest
)
