package io.provenance.api.models.cee.submit

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig

data class SubmitContractBatchExecutionResultRequest(
    val account: AccountInfo = AccountInfo(),
    val provenance: ProvenanceConfig,
    val submission: List<EnvelopeSubmission>,
    val chunkSize: Int = 25,
)
