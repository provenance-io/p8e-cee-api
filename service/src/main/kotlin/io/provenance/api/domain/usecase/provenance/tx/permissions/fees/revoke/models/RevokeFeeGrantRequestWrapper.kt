package io.provenance.api.domain.usecase.provenance.tx.permissions.fees.revoke.models

import io.provenance.api.models.p8e.tx.permissions.fees.revoke.RevokeFeeGrantRequest
import java.util.UUID

class RevokeFeeGrantRequestWrapper(
    val uuid: UUID,
    val request: RevokeFeeGrantRequest
)
