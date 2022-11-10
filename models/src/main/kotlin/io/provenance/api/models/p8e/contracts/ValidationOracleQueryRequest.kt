package io.provenance.api.models.p8e.contracts

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig

data class ValidationOracleQueryRequest(
    val provenanceConfig: ProvenanceConfig,
    val contractConfig: VOSmartContractConfig,
    val account: AccountInfo = AccountInfo(),
    val libraryCall: VOSmartContractLibraryClientCall
)
