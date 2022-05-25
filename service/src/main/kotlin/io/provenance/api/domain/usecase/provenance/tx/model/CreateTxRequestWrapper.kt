package io.provenance.api.domain.usecase.provenance.tx.model

import io.provenance.api.models.p8e.CreateTxRequest
import java.util.UUID

data class CreateTxRequestWrapper(
    val uuid: UUID,
    val request: CreateTxRequest
)
