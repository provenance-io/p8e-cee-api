package io.provenance.api.domain.usecase.cee.reject.models

import io.provenance.api.models.cee.reject.RejectContractBatchRequest
import java.util.UUID

class RejectContractBatchRequestWrapper(
    val uuid: UUID,
    val request: RejectContractBatchRequest,
)
