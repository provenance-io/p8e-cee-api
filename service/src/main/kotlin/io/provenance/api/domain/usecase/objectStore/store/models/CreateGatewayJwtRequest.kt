package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.account.KeyManagementConfig
import io.provenance.api.models.entity.EntityID

data class CreateGatewayJwtRequest(
    val entityID: EntityID,
    val keyManagementConfig: KeyManagementConfig?
)
