package io.provenance.api.domain.usecase.provenance.tx.execute.models

import io.provenance.api.models.user.EntityID
import io.provenance.api.models.p8e.tx.ExecuteTxRequest

data class ExecuteTxRequestWrapper(
    val entityID: EntityID,
    val request: ExecuteTxRequest
)
