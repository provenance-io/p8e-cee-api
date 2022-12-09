package io.provenance.api.frameworks.web.misc

import com.google.common.util.concurrent.RateLimiter
import io.provenance.api.frameworks.config.RateLimiterProps
import java.util.concurrent.TimeUnit
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.CoRouterFunctionDsl
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter

fun CoRouterFunctionDsl.rateLimited(limiter: RateLimiter, rateLimiterProps: RateLimiterProps) {
    filter { request, handler ->
        if (limiter.tryAcquire(rateLimiterProps.permitsToAcquire, rateLimiterProps.backoffSeconds, TimeUnit.SECONDS)) {
            handler(request)
        } else {
            ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS).buildAndAwait()
        }
    }
}

fun rateLimitedCoRouter(rateLimiter: RateLimiter, rateLimiterProps: RateLimiterProps, routes: (CoRouterFunctionDsl.() -> Unit)) = coRouter {
    rateLimited(rateLimiter, rateLimiterProps)
    apply(routes)
}
