package io.provenance.onboarding.domain.usecase.provenance.tx.model

import io.provenance.api.models.p8e.ExecuteTxRequest
import java.util.UUID

data class ExecuteTxRequestWrapper(
    val uuid: UUID,
    val request: ExecuteTxRequest
)
