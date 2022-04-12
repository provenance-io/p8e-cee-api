package io.provenance.onboarding.frameworks.web.misc

import mu.KotlinLogging
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import java.util.UUID

private val log = KotlinLogging.logger {}

val ServerRequest.ipAddress: String
    get() = headers().firstHeader("x-real-ip") ?: remoteAddress().get().address.toString()

suspend inline fun <reified T> ServerRequest.requireBody(): T =
    if (T::class == Unit::class) {
        Unit as T
    } else {
        requireNotNull(awaitBodyOrNull()) { "Failed to parse request body of type ${T::class}" }
    }

fun ServerRequest.getUser(): UUID {
    try {
        val header = requireNotNull(headers().firstHeader("x-uuid"))
        return UUID.fromString(header)
    } catch (exception: IllegalArgumentException) {
        log.error(exception) { "error parsing x-uuid header" }
        throw IllegalArgumentException("error parsing x-uuid header", exception)
    }
}
