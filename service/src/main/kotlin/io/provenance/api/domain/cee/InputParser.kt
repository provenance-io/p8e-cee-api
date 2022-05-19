package io.provenance.api.domain.cee

import com.google.protobuf.Message

interface InputParser {
    val type: Class<*>
    fun parse(input: Any, type: Class<*>): Message
}
