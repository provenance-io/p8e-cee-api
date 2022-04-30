package io.provenance.onboarding.domain.usecase.cee.submit.model

import io.provenance.onboarding.domain.usecase.common.model.AccountInfo
import io.provenance.onboarding.domain.usecase.common.model.ProvenanceConfig

data class SubmitContractExecutionResultRequest(
    val account: AccountInfo,
    val provenance: ProvenanceConfig,
    val envelope: Any,
    val state: Any,
)
