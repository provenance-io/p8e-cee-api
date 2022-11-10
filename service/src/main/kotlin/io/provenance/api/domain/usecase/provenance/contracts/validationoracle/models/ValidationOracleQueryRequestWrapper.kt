package io.provenance.api.domain.usecase.provenance.contracts.validationoracle.models

import io.provenance.api.models.p8e.contracts.ValidationOracleQueryRequest
import java.util.UUID

data class ValidationOracleQueryRequestWrapper(
    val uuid: UUID,
    val request: ValidationOracleQueryRequest
)
