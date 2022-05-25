package io.provenance.api.domain.cee

import com.google.protobuf.Message

interface InputParser {
    val type: Class<*>
    val default: Boolean
    fun parse(input: Any, type: Class<*>, includeTypes: List<String> = emptyList()): Message
}
