package io.provenance.api.domain.usecase.cee.execute.model

import io.provenance.api.models.cee.execute.ExecuteContractBatchRequest
import java.util.UUID

data class ExecuteContractBatchRequestWrapper(
    val uuid: UUID,
    val request: ExecuteContractBatchRequest,
)
