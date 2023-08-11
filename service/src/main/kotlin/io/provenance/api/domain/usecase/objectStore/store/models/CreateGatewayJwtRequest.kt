package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.account.KeyManagementConfig
import io.provenance.api.models.entity.Entity

data class CreateGatewayJwtRequest(
    val entity: Entity,
    val keyManagementConfig: KeyManagementConfig?
)
