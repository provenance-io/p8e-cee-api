package io.provenance.api.frameworks.web

import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait

object SuccessResponses {

    suspend fun noContent() = ServerResponse.noContent().buildAndAwait()

    suspend fun ok(body: Any) = ServerResponse.ok().bodyValueAndAwait(body)
}
