package io.provenance.api.models.p8e.contracts

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig

data class VerifyAssetRequest(
    val provenanceConfig: ProvenanceConfig,
    val contractConfig: SmartContractConfig,
    val account: AccountInfo = AccountInfo(),
    val message: String,
    val success: Boolean
)
