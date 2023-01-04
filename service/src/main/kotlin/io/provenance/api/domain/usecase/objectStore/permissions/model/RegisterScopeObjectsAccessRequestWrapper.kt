package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.eos.permissions.RegisterScopeObjectsAccessRequest
import java.util.UUID

data class RegisterScopeObjectsAccessRequestWrapper(
    val uuid: UUID,
    val request: RegisterScopeObjectsAccessRequest
)
