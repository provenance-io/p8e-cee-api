package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.account.KeyManagementConfig
import java.util.UUID

data class CreateGatewayJwtRequest(
    val uuid: UUID,
    val keyManagementConfig: KeyManagementConfig?
)
