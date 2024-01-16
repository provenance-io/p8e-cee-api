package io.provenance.api.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hubspot.jackson.datatype.protobuf.ProtobufModule

private val debugObjectMapper by lazy {
    jacksonObjectMapper()
        .registerKotlinModule()
        .registerModule(ProtobufModule())
}

fun Any.toPrettyJson(): String = debugObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)

fun buildLogMessage(message: String, args: List<Pair<String, Any?>>): String =
    args.joinToString(
        separator = ", ",
        prefix = "[",
        postfix = "]"
    ) { "${it.first}=${it.second}" }.let { argString ->
        "$message $argString".trim()
    }

fun buildLogMessage(message: String, vararg args: Pair<String, Any?>): String = buildLogMessage(message, args.toList())
