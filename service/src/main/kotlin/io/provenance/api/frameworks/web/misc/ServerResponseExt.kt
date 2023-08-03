package io.provenance.api.frameworks.web.misc

import io.provenance.api.frameworks.web.ErrorResponses
import io.provenance.api.frameworks.web.SuccessResponses
import mu.KotlinLogging
import org.springframework.http.HttpEntity
import org.springframework.web.reactive.function.server.ServerResponse

private val log = KotlinLogging.logger {}

suspend fun Result<Any>.foldToServerResponse(): ServerResponse =
    fold(
        onSuccess = {
            if (it is Unit) {
                SuccessResponses.noContent()
            } else {
                when (it) {
                    is HttpEntity<*> -> {
                        SuccessResponses.okWithHeaders(it.headers, it.body!!)
                    }
                    else -> SuccessResponses.ok(it)
                }
            }
        },
        onFailure = {
            log.warn(it) { "returning error response for ${it::class}" }
            ErrorResponses.defaultForType(it)
        }
    )

suspend fun respond(block: () -> Any) = runCatching { block() }.foldToServerResponse()
