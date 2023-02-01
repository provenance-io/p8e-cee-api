package io.provenance.api.domain.usecase.provenance.tx.permissions.fees.get.models

import io.provenance.api.models.p8e.tx.permissions.fees.get.GetFeeGrantAllowanceRequest
import java.util.UUID

data class GetFeeGrantAllowanceRequestWrapper(
    val uuid: UUID,
    val request: GetFeeGrantAllowanceRequest
)
