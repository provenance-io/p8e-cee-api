package io.provenance.api.models.cee

import io.provenance.api.models.eos.ObjectStoreConfig
import io.provenance.api.models.account.AccountInfo

data class RejectContractRequest(
    val account: AccountInfo,
    val client: ObjectStoreConfig,
    val rejection: ByteArray,
)
