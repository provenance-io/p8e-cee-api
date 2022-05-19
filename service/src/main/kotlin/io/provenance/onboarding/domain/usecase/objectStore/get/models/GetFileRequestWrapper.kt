package io.provenance.onboarding.domain.usecase.objectStore.get.models

import io.provenance.api.models.eos.GetFileRequest
import java.util.UUID

data class GetFileRequestWrapper(
    val uuid: UUID,
    val request: GetFileRequest
)
