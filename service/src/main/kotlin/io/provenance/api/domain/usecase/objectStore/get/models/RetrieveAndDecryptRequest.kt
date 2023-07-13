package io.provenance.api.domain.usecase.objectStore.get.models

import io.provenance.api.models.account.KeyManagementConfig
import io.provenance.api.models.user.UserID
import java.util.UUID

data class RetrieveAndDecryptRequest(
    val userID: UserID,
    val objectStoreAddress: String,
    val hash: String,
    val keyManagementConfig: KeyManagementConfig?,
    val useObjectStoreGateway: Boolean = false
)
