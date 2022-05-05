package io.provenance.cee.api.models.cee

import io.provenance.cee.api.models.account.AccountInfo
import io.provenance.cee.api.models.p8e.ProvenanceConfig

data class SubmitContractExecutionResultRequest(
    val account: AccountInfo,
    val provenance: ProvenanceConfig,
    val envelope: ByteArray,
    val state: ByteArray,
)
