package io.provenance.cee.api.models.cee

import io.provenance.cee.api.models.eos.ObjectStoreConfig
import io.provenance.cee.api.models.account.AccountInfo
import io.provenance.cee.api.models.p8e.ProvenanceConfig

data class ExecuteContractConfig(
    val contract: ContractConfig,
    val client: ObjectStoreConfig,
    val account: AccountInfo,
    val provenanceConfig: ProvenanceConfig,
)
