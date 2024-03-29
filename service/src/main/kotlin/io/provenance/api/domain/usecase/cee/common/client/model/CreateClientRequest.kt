package io.provenance.api.domain.usecase.cee.common.client.model

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.ObjectStoreConfig
import io.provenance.api.models.p8e.AudienceKeyPair

data class CreateClientRequest(
    val entity: Entity,
    val account: AccountInfo = AccountInfo(),
    val client: ObjectStoreConfig,
    val affiliates: Set<AudienceKeyPair> = emptySet()
)
