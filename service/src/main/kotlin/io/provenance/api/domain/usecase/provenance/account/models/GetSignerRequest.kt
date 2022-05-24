package io.provenance.api.domain.usecase.provenance.account.models

import io.provenance.api.models.account.AccountInfo
import java.util.UUID

data class GetSignerRequest(
    val uuid: UUID,
    val account: AccountInfo
)
