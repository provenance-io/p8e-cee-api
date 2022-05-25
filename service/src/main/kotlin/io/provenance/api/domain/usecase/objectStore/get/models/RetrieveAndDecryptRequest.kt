package io.provenance.api.domain.usecase.objectStore.get.models

import io.provenance.api.models.account.KeyManagementConfig
import java.util.UUID

data class RetrieveAndDecryptRequest(
    val uuid: UUID,
    val objectStoreAddress: String,
    val hash: String,
    val keyManagementConfig: KeyManagementConfig?,
)
