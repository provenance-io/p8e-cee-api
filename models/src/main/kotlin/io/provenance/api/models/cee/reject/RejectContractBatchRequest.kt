package io.provenance.api.models.cee.reject

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.ObjectStoreConfig

data class RejectContractBatchRequest(
    val account: AccountInfo = AccountInfo(),
    val client: ObjectStoreConfig,
    val rejection: List<ByteArray>,
)
