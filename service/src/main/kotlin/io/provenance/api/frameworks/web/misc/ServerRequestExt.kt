package io.provenance.api.frameworks.web.misc

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.entity.KongConsumer
import io.provenance.api.models.entity.MemberUUID
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

fun ServerRequest.getEntity(): Entity {
    try {
        return requireNotNull(getConsumerOrNull() ?: getMemberUUIDOrNull())
    } catch (exception: IllegalArgumentException) {
        log.error(exception) { "error parsing x-uuid or address header" }
        throw IllegalArgumentException("error parsing x-uuid or address header", exception)
    }
}

fun ServerRequest.getMemberUUIDOrNull(): MemberUUID? = headers().firstHeader("x-uuid")
    ?.let { MemberUUID(UUID.fromString(it)) }

fun ServerRequest.getConsumerOrNull(): KongConsumer? {
    return headers().firstHeader("x-consumer-id")?.let { id ->
        KongConsumer(
            id = id,
            username = headers().firstHeader("x-consumer-username"),
            customId = requireNotNull(headers().firstHeader("x-consumer-custom-id")) {
                "Custom ID for consumer missing"
            },
        )
    }
}
