package io.provenance.api.domain.usecase.objectStore.permissions.model

import io.provenance.api.models.eos.permissions.RegisterObjectAccessRequest
import java.util.UUID

data class RegisterObjectAccessRequestWrapper(
    val uuid: UUID,
    val request: RegisterObjectAccessRequest
)
