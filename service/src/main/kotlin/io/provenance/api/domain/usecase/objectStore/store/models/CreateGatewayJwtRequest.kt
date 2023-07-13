package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.account.KeyManagementConfig
import io.provenance.api.models.user.UserID

data class CreateGatewayJwtRequest(
    val userID: UserID,
    val keyManagementConfig: KeyManagementConfig?
)
