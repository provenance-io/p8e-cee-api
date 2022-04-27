package io.provenance.onboarding.domain.usecase.cee.model

import io.provenance.onboarding.domain.usecase.common.model.AccountInfo
import io.provenance.onboarding.domain.usecase.common.model.ProvenanceConfig

data class ExecuteContractConfig(
    val contract: ContractConfig,
    val client: ClientConfig,
    val account: AccountInfo,
    val provenanceConfig: ProvenanceConfig,
)
