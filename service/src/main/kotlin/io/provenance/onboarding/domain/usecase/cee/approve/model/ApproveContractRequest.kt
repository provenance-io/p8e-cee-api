package io.provenance.onboarding.domain.usecase.cee.approve.model

import io.provenance.onboarding.domain.usecase.cee.common.model.ClientConfig
import io.provenance.onboarding.domain.usecase.common.model.AccountInfo
import io.provenance.onboarding.domain.usecase.common.model.ProvenanceConfig
import java.time.OffsetDateTime

data class ApproveContractRequest(
    val account: AccountInfo,
    val client: ClientConfig,
    val provenanceConfig: ProvenanceConfig,
    val envelopeState: Any,
    val expiration: OffsetDateTime = OffsetDateTime.now().plusHours(1),
)
