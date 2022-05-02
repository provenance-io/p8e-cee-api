package io.provenance.onboarding.domain.usecase.cee.common.client.model

import io.provenance.onboarding.domain.usecase.cee.common.model.ClientConfig
import io.provenance.onboarding.domain.usecase.common.model.AccountInfo
import io.provenance.scope.sdk.Affiliate

data class CreateClientRequest(
    val account: AccountInfo,
    val client: ClientConfig,
    val affiliates: List<AccountInfo> = emptyList(),
)
