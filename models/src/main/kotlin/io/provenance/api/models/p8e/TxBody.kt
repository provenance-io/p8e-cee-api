package io.provenance.api.models.p8e

import com.fasterxml.jackson.databind.node.ObjectNode

data class TxBody(
    val json: ObjectNode,
    val base64: List<String>
)
