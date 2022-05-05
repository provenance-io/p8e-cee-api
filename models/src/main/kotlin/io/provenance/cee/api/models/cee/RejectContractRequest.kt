package io.provenance.cee.api.models.cee

import io.provenance.cee.api.models.eos.ObjectStoreConfig
import io.provenance.cee.api.models.account.AccountInfo

data class RejectContractRequest(
    val account: AccountInfo,
    val client: ObjectStoreConfig,
    val rejection: ByteArray,
)
