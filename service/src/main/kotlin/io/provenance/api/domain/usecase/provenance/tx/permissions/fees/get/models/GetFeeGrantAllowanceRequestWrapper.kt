package io.provenance.api.domain.usecase.provenance.tx.permissions.fees.get.models

import io.provenance.api.models.p8e.tx.permissions.fees.get.GetFeeGrantAllowanceRequest
import io.provenance.api.models.entity.Entity

data class GetFeeGrantAllowanceRequestWrapper(
    val entity: Entity,
    val request: GetFeeGrantAllowanceRequest
)
