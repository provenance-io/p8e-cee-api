package io.provenance.api.models.account

import io.provenance.scope.contract.proto.Specifications

data class AccountInfo(
    val keyRingIndex: Int = 0,
    val keyIndex: Int = 0,
    val partyType: Specifications.PartyType = Specifications.PartyType.OWNER,
    val keyManagementConfig: KeyManagementConfig? = null,
)
