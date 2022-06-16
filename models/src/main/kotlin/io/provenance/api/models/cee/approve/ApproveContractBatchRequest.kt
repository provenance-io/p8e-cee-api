package io.provenance.api.models.cee.approve

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.ObjectStoreConfig
import io.provenance.api.models.p8e.ProvenanceConfig

class ApproveContractBatchRequest(
    val account: AccountInfo = AccountInfo(),
    val client: ObjectStoreConfig,
    val provenanceConfig: ProvenanceConfig,
    val approvals: List<EnvelopeApproval>,
    val chunkSize: Int = 25,
)
