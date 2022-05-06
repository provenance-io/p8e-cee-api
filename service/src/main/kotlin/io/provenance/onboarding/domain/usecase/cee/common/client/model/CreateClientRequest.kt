package io.provenance.onboarding.domain.usecase.cee.common.client.model

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.ObjectStoreConfig

data class CreateClientRequest(
    val account: AccountInfo,
    val client: ObjectStoreConfig,
    val affiliates: List<AccountInfo> = emptyList(),
)
