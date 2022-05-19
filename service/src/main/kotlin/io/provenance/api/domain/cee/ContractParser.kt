package io.provenance.api.domain.cee

import com.google.protobuf.Message

interface ContractParser {
    fun parseInput(input: Any, type: Class<*>): Message
}
