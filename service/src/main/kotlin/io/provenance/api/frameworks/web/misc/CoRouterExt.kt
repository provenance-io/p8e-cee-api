package io.provenance.api.frameworks.web.misc

import com.google.common.util.concurrent.RateLimiter
import java.util.concurrent.TimeUnit
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.CoRouterFunctionDsl
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter

fun CoRouterFunctionDsl.rateLimited(limiter: RateLimiter) {
    filter { request, handler ->
        if (limiter.tryAcquire(1, 10, TimeUnit.SECONDS)) {
            handler(request)
        } else {
            ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS).buildAndAwait()
        }
    }
}

fun rateLimitedCoRouter(rateLimiter: RateLimiter, routes: (CoRouterFunctionDsl.() -> Unit)) = coRouter {
    rateLimited(rateLimiter)
    apply(routes)
}
