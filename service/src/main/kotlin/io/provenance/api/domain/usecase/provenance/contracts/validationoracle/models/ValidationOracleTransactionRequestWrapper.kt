package io.provenance.api.domain.usecase.provenance.contracts.validationoracle.models

import io.provenance.api.models.p8e.contracts.ValidationOracleTransactionRequest
import java.util.UUID

data class ValidationOracleTransactionRequestWrapper(
    val uuid: UUID,
    val request: ValidationOracleTransactionRequest
)
