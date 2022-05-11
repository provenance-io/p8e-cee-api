package io.provenance.api.models.cee

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.ObjectStoreConfig

data class RejectContractRequest(
    val account: AccountInfo = AccountInfo(),
    val client: ObjectStoreConfig,
    val rejection: ByteArray,
)
