package io.provenance.api.domain.usecase.provenance.tx.permissions.fees.create.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.p8e.tx.permissions.fees.grant.GrantFeeGrantRequest

data class GrantFeeGrantRequestWrapper(
    val entity: Entity,
    val request: GrantFeeGrantRequest
)
