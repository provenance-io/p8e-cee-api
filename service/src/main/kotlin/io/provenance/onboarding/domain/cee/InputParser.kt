package io.provenance.onboarding.domain.cee

import com.google.protobuf.Message

interface InputParser {
    val type: Class<*>
    fun parse(input: Any, type: Class<*>, includeTypes: List<String> = emptyList()): Message
}
