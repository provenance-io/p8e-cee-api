package io.provenance.api.domain.usecase.objectStore.get.models

import io.provenance.api.models.account.KeyManagementConfig
import io.provenance.api.models.entity.Entity

data class RetrieveAndDecryptRequest(
    val entity: Entity,
    val objectStoreAddress: String,
    val hash: String,
    val keyManagementConfig: KeyManagementConfig?,
    val useObjectStoreGateway: Boolean = false
)
