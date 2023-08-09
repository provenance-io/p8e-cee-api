package io.provenance.api.frameworks.web.misc

import io.provenance.api.models.entity.Address
import io.provenance.api.models.entity.Consumer
import io.provenance.api.models.entity.EntityID
import io.provenance.api.models.entity.UserUUID
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

fun ServerRequest.getEntityID(): EntityID {
    try {
        return requireNotNull(getAddressOrNull() ?: getConsumerOrNull() ?: getUUIDOrNull())
    } catch (exception: IllegalArgumentException) {
        log.error(exception) { "error parsing x-uuid or address header" }
        throw IllegalArgumentException("error parsing x-uuid or address header", exception)
    }
}

fun ServerRequest.getUUIDOrNull(): UserUUID? = headers().firstHeader("x-uuid")
    ?.let { UserUUID(UUID.fromString(it)) }

fun ServerRequest.getAddressOrNull(): Address? {
    val address = headers().firstHeader("x-figure-tech-address")?.also {
        log.info { "x-figure-tech-address header = $it" }
    } ?: headers().firstHeader("x-figure-tech-granter-address")?.also {
        log.info { "x-figure-tech-granter-address header = $it" }
    }
    return address?.let { Address(it) }
}

fun ServerRequest.getConsumerOrNull(): Consumer? {
    return Consumer(
        id = headers().firstHeader("x-consumer-id"),
        username = headers().firstHeader("x-consumer-username"),
        customId = headers().firstHeader("x-consumer-custom-id"),
    ).takeUnless { it.id == null && it.username == null && it.customId == null }
        ?.also { log.info { "consumer headers $it" } }
}
