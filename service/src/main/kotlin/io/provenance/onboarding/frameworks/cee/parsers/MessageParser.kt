package io.provenance.onboarding.frameworks.cee.parsers

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import io.provenance.onboarding.domain.cee.InputParser
import io.provenance.onboarding.util.toPrettyJson
import org.springframework.stereotype.Component

@Component
class MessageParser : InputParser {
    override val type: Class<*> = Message::class.java

    override fun parse(input: Any, type: Class<*>): Message {
        val builder = type.getMethod("newBuilder").invoke(null) as Message.Builder
        JsonFormat.parser().merge(input.toPrettyJson(), builder)
        return builder.build()
    }
}
