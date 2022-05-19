package io.provenance.api.domain.usecase.cee.common.client.model

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.account.Participant
import io.provenance.api.models.eos.ObjectStoreConfig
import java.util.UUID

data class CreateClientRequest(
    val uuid: UUID,
    val account: AccountInfo = AccountInfo(),
    val client: ObjectStoreConfig,
    val affiliates: List<Participant> = emptyList(),
)
