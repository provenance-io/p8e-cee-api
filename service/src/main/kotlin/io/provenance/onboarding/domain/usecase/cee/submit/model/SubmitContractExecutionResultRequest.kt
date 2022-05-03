package io.provenance.onboarding.domain.usecase.cee.submit.model

import io.provenance.onboarding.domain.usecase.common.model.AccountInfo
import io.provenance.onboarding.domain.usecase.common.model.ProvenanceConfig
import io.provenance.scope.contract.proto.Contracts
import io.provenance.scope.sdk.ExecutionResult

data class SubmitContractExecutionResultRequest(
    val account: AccountInfo,
    val provenance: ProvenanceConfig,
    val envelope: ByteArray,
    val state: ByteArray,
)
