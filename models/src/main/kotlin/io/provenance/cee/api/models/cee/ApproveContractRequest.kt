package io.provenance.cee.api.models.cee

import io.provenance.cee.api.models.eos.ObjectStoreConfig
import io.provenance.cee.api.models.account.AccountInfo
import io.provenance.cee.api.models.p8e.ProvenanceConfig
import java.time.OffsetDateTime

data class ApproveContractRequest(
    val account: AccountInfo,
    val client: ObjectStoreConfig,
    val provenanceConfig: ProvenanceConfig,
    val envelope: ByteArray,
    val expiration: OffsetDateTime = OffsetDateTime.now().plusHours(1),
)
