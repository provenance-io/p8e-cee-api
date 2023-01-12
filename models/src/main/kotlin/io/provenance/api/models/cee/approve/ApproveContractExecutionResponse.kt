package io.provenance.api.models.cee.approve

import io.provenance.api.models.p8e.TxResponse
import java.util.UUID

data class ApproveContractExecutionResponse(
    val envelopeStateBase64EncodedByteArray: String?,
    val tx: TxResponse?,
    val associatedScopeUuids: List<UUID>
)
