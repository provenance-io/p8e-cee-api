package io.provenance.api.models.account

import io.provenance.scope.contract.proto.Specifications
import java.util.UUID

data class Participant(
    val uuid: UUID,
    val partyType: Specifications.PartyType,
)
