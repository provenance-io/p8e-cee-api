package io.provenance.api.domain.usecase.provenance.contracts.validationoracle.models

import io.provenance.api.models.p8e.contracts.SmartContractTransactionRequest
import java.util.UUID

data class SmartContractTransactionRequestWrapper(
    val uuid: UUID,
    val request: SmartContractTransactionRequest
)
