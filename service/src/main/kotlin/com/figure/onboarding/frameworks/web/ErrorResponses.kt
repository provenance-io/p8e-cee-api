package com.figure.onboarding.frameworks.web

import com.figure.onboarding.domain.usecase.common.errors.NotFoundError
import com.figure.onboarding.domain.usecase.common.errors.ServerError
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait

private val log = KotlinLogging.logger {}

object ErrorResponses {
    private fun logError(cause: Throwable) {
        val rootCause = cause.cause ?: cause
        log.error(cause.localizedMessage)
        log.error(rootCause.stackTraceToString())
    }

    private suspend fun badRequest(cause: Throwable) =
        ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValueAndAwait(cause.localizedMessage)

    private suspend fun notFound(cause: Throwable) =
        ServerResponse.status(HttpStatus.NOT_FOUND).bodyValueAndAwait(cause.localizedMessage)

    private suspend fun serverError(cause: Throwable) =
        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValueAndAwait(cause.localizedMessage)

    private suspend fun unknownError(cause: Throwable) =
        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValueAndAwait(cause.localizedMessage)

    suspend fun defaultForType(cause: Throwable): ServerResponse {
        logError(cause)
        return when (cause) {
            is NotFoundError -> notFound(cause)
            is IllegalArgumentException -> badRequest(cause)
            is ServerError -> serverError(cause)
            else -> unknownError(cause)
        }
    }
}
