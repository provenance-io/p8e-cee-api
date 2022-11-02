package io.provenance.api.models.p8e.contracts

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.ProvenanceConfig
import java.math.BigInteger

data class ValidationOracleTransactionRequest(
    val provenanceConfig: ProvenanceConfig,
    val contractConfig: SmartContractConfig,
    val account: AccountInfo = AccountInfo(),
    val json: String
)
