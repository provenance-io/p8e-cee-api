package io.provenance.api.domain.usecase.provenance.tx.permissions.fees.revoke.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.p8e.tx.permissions.fees.revoke.RevokeFeeGrantRequest

class RevokeFeeGrantRequestWrapper(
    val userID: UserID,
    val request: RevokeFeeGrantRequest
)
