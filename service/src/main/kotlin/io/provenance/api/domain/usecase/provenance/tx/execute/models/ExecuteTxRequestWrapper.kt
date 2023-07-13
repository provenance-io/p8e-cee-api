package io.provenance.api.domain.usecase.provenance.tx.execute.models

import io.provenance.api.models.user.UserID
import io.provenance.api.models.p8e.tx.ExecuteTxRequest

data class ExecuteTxRequestWrapper(
    val userID: UserID,
    val request: ExecuteTxRequest
)
