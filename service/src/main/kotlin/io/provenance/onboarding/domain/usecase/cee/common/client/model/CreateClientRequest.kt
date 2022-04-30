package io.provenance.onboarding.domain.usecase.cee.common.client.model

import io.provenance.onboarding.domain.usecase.cee.common.model.ClientConfig
import io.provenance.onboarding.domain.usecase.common.model.AccountInfo

data class CreateClientRequest(
    val account: AccountInfo,
    val client: ClientConfig
)
