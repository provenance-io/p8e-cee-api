package io.provenance.api.domain.usecase.provenance.contracts.validationoracle.models

import io.provenance.api.models.p8e.contracts.SmartContractQueryRequest
import java.util.UUID

data class SmartContractQueryRequestWrapper(
    val uuid: UUID,
    val request: SmartContractQueryRequest
)
