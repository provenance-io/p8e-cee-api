package io.provenance.onboarding.domain.usecase.common.model

import io.provenance.scope.contract.proto.Specifications
import java.util.UUID

data class AccountInfo(
    val originatorUuid: UUID,
    val keyRingIndex: Int = 0,
    val keyIndex: Int = 0,
    val isTestNet: Boolean = true,
    val partyType: Specifications.PartyType = Specifications.PartyType.OWNER,
)
