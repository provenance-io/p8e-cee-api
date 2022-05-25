package io.provenance.onboarding.frameworks.cee.parsers

import com.google.protobuf.Descriptors
import com.google.protobuf.Message
import com.google.protobuf.TypeRegistry
import com.google.protobuf.util.JsonFormat
import io.provenance.onboarding.domain.cee.InputParser
import io.provenance.onboarding.util.toPrettyJson
import org.springframework.stereotype.Component

@Component
class JsonMessageParser(
    override val default: Boolean = false
) : InputParser {
    override val type: Class<*> = Message::class.java

    override fun parse(input: Any, type: Class<*>, includeTypes: List<String>): Message {
        val builder = type.getMethod("newBuilder").invoke(null) as Message.Builder
        val typeRegistry = TypeRegistry.newBuilder()

        includeTypes.forEach {
            val descriptor = Class.forName(it).getMethod("getDescriptor").invoke(null) as Descriptors.Descriptor
            typeRegistry.add(descriptor)
        }

        JsonFormat.parser().usingTypeRegistry(typeRegistry.build()).merge(input.toPrettyJson(), builder)
        return builder.build()
    }
}
