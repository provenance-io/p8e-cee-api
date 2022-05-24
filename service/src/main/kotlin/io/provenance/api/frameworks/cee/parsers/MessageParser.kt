package io.provenance.api.frameworks.cee.parsers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.protobuf.Message
import com.hubspot.jackson.datatype.protobuf.ProtobufModule
import io.provenance.api.domain.cee.InputParser
import io.provenance.api.util.toPrettyJson
import org.springframework.stereotype.Component

@Component
class MessageParser(
    override val default: Boolean = true
) : InputParser {
    override val type: Class<*> = Message::class.java

    override fun parse(input: Any, type: Class<*>, includeTypes: List<String>): Message = ObjectMapper()
        .registerModule(ProtobufModule())
        .registerModule(JavaTimeModule())
        .readValue(input.toPrettyJson(), type) as Message
}
