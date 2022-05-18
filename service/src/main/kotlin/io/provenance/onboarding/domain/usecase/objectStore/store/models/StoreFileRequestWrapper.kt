package io.provenance.onboarding.domain.usecase.objectStore.store.models

import io.provenance.api.models.eos.StoreProtoRequest
import java.util.UUID
import org.springframework.http.codec.multipart.Part
import org.springframework.util.MultiValueMap

data class StoreFileRequestWrapper(
    val uuid: UUID,
    val request: Map<String, Part>,
)
