package io.provenance.api.domain.usecase.provenance.tx.execute.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.p8e.tx.ExecuteTxRequest

data class ExecuteTxRequestWrapper(
    val entity: Entity,
    val request: ExecuteTxRequest
)
