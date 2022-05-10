package io.provenance.api.models.cee

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.ObjectStoreConfig
import io.provenance.api.models.p8e.ProvenanceConfig
import java.time.OffsetDateTime

data class ApproveContractRequest(
    val account: AccountInfo,
    val client: ObjectStoreConfig,
    val provenanceConfig: ProvenanceConfig,
    val envelope: ByteArray,
    val expiration: OffsetDateTime = OffsetDateTime.now().plusHours(1),
)
