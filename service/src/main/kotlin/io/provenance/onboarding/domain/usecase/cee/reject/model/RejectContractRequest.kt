package io.provenance.onboarding.domain.usecase.cee.reject.model

import io.provenance.onboarding.domain.usecase.cee.common.model.ClientConfig
import io.provenance.onboarding.domain.usecase.common.model.AccountInfo
import java.time.OffsetDateTime

data class RejectContractRequest(
    val account: AccountInfo,
    val client: ClientConfig,
    val rejection: ByteArray,
)
