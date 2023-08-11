package io.provenance.api.domain.usecase.cee.approve.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.cee.approve.ApproveContractRequest

data class ApproveContractRequestWrapper(
    val entity: Entity,
    val request: ApproveContractRequest
)
