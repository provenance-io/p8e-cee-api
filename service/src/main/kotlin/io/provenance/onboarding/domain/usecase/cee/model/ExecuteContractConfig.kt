package io.provenance.onboarding.domain.usecase.cee.model

import io.provenance.onboarding.domain.usecase.common.model.AccountInfo
import java.util.UUID

data class ExecuteContractConfig(
    val contractName: String,
    val scopeUuid: UUID,
    val sessionUuid: UUID,
    val objectStoreUrl: String,
    val partyType: String,
    val isTestNet: Boolean,
    val account: AccountInfo,
    val chainId: String,
    val nodeEndpoint: String,
)
