package io.provenance.api.models.cee.execute

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.cee.ContractConfig
import io.provenance.api.models.eos.ObjectStoreConfig
import io.provenance.api.models.p8e.ProvenanceConfig

data class ExecuteContractConfig(
    val contract: ContractConfig,
    val client: ObjectStoreConfig,
    val account: AccountInfo = AccountInfo(),
    val provenanceConfig: ProvenanceConfig,
)
