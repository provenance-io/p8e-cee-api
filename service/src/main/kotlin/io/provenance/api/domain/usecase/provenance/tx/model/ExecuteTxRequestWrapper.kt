package io.provenance.api.domain.usecase.provenance.tx.model

import io.provenance.api.models.p8e.tx.ExecuteTxRequest
import java.util.UUID

data class ExecuteTxRequestWrapper(
    val uuid: UUID,
    val request: ExecuteTxRequest
)
