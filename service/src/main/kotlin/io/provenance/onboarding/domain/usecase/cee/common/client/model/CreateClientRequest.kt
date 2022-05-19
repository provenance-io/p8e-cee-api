package io.provenance.onboarding.domain.usecase.cee.common.client.model

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.ObjectStoreConfig
import io.provenance.api.models.p8e.AudienceKeyPair
import java.util.UUID

data class CreateClientRequest(
    val uuid: UUID,
    val account: AccountInfo = AccountInfo(),
    val client: ObjectStoreConfig,
    val affiliates: Set<AudienceKeyPair> = emptySet()
)
