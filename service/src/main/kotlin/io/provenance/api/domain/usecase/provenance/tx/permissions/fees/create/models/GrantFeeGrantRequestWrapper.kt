package io.provenance.api.domain.usecase.provenance.tx.permissions.fees.create.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.p8e.tx.permissions.fees.grant.GrantFeeGrantRequest

data class GrantFeeGrantRequestWrapper(
    val userID: UserID,
    val request: GrantFeeGrantRequest
)
