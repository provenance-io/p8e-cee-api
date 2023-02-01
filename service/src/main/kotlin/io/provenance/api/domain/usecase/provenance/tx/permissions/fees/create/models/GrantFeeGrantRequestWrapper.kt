package io.provenance.api.domain.usecase.provenance.tx.permissions.fees.create.models

import io.provenance.api.models.p8e.tx.permissions.fees.grant.GrantFeeGrantRequest
import java.util.UUID

data class GrantFeeGrantRequestWrapper(
    val uuid: UUID,
    val request: GrantFeeGrantRequest
)
