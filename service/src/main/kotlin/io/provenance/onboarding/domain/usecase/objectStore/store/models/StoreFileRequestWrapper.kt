package io.provenance.onboarding.domain.usecase.objectStore.store.models

import java.util.UUID
import org.springframework.http.codec.multipart.Part

data class StoreFileRequestWrapper(
    val uuid: UUID,
    val request: Map<String, Part>,
)
