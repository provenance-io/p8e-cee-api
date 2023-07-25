package io.provenance.api.domain.usecase.cee.approve.models

import io.provenance.api.models.user.EntityID
import io.provenance.api.models.cee.approve.ApproveContractRequest

data class ApproveContractRequestWrapper(
    val entityID: EntityID,
    val request: ApproveContractRequest
)
