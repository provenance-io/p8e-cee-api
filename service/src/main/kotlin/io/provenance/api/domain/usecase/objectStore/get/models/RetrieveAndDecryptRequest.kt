package io.provenance.api.domain.usecase.objectStore.get.models

import io.provenance.api.models.account.KeyManagementConfig
import io.provenance.api.models.entity.EntityID

data class RetrieveAndDecryptRequest(
    val entityID: EntityID,
    val objectStoreAddress: String,
    val hash: String,
    val keyManagementConfig: KeyManagementConfig?,
    val useObjectStoreGateway: Boolean = false
)
