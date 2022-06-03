package io.provenance.api.models.p8e.contracts

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig

data class ClassifyAssetRequest(
    val provenanceConfig: ProvenanceConfig,
    val contractConfig: SmartContractConfig,
    val account: AccountInfo = AccountInfo(),
)
