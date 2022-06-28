package io.provenance.api.domain.usecase.provenance.tx.create.models

import io.provenance.api.models.p8e.tx.CreateTxRequest
import java.util.UUID

data class CreateTxRequestWrapper(
    val uuid: UUID,
    val request: CreateTxRequest
)
