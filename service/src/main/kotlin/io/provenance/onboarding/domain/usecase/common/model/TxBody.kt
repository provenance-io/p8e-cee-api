package io.provenance.onboarding.domain.usecase.common.model

import com.fasterxml.jackson.databind.node.ObjectNode

data class TxBody(
    val json: ObjectNode,
    val base64: List<String>
)
