package io.provenance.api.models.p8e.contracts

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig

data class SmartContractTransactionRequest(
    val provenanceConfig: ProvenanceConfig,
    val contractConfig: SmartContractConfiguration,
    val account: AccountInfo = AccountInfo(),
    val libraryInvocation: SmartContractClientLibraryInvocation
)
