package io.provenance.api.domain.usecase.common.originator.models

import io.provenance.api.models.account.KeyManagementConfig

data class KeyManagementConfigWrapper(
    val entity: String,
    val config: KeyManagementConfig?
)
