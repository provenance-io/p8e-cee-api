package io.provenance.api.domain.usecase.common.originator.models

import io.provenance.api.models.account.KeyManagementConfig
import java.util.UUID

data class KeyManagementConfigWrapper(
    val uuid: UUID,
    val config: KeyManagementConfig
)
