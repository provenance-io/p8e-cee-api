package com.figure.onboarding.frameworks.web.misc

import com.figure.onboarding.frameworks.web.ErrorResponses
import com.figure.onboarding.frameworks.web.SuccessResponses
import mu.KotlinLogging
import org.springframework.web.reactive.function.server.ServerResponse

private val log = KotlinLogging.logger {}

suspend fun Result<Any>.foldToServerResponse(): ServerResponse =
    fold(
        onSuccess = {
            if (it is Unit) {
                SuccessResponses.noContent()
            } else {
                SuccessResponses.ok(it)
            }
        },
        onFailure = {
            log.warn(it) { "returning error response for ${it::class}" }
            ErrorResponses.defaultForType(it)
        }
    )
