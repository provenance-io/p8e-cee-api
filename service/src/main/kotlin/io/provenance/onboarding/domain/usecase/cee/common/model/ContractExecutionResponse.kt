package io.provenance.onboarding.domain.usecase.cee.common.model

import io.provenance.onboarding.domain.usecase.common.model.TxResponse

data class ContractExecutionResponse(
    val multiparty: Boolean,
    val metadata: TxResponse?
)
