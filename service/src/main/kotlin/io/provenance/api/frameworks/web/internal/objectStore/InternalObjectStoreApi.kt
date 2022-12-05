package io.provenance.api.frameworks.web.internal.objectStore

import com.google.common.util.concurrent.RateLimiter
import io.provenance.api.frameworks.web.Routes
import io.provenance.api.frameworks.web.logging.logExchange
import io.provenance.api.frameworks.web.misc.rateLimitedCoRouter
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val log = KotlinLogging.logger {}

@Configuration
class InternalObjectStoreApi(
    private val rateLimiter: RateLimiter
) {
    @Bean
    fun internalObjectStoreApiV1(handler: InternalObjectStoreHandler) = rateLimitedCoRouter(rateLimiter) {
        logExchange(log)
        Routes.INTERNAL_BASE_V1.nest {
            "/eos".nest {
                POST("", handler::storeProto)
                GET("", handler::getProto)
                POST("/file", handler::storeFile)
                GET("/file", handler::getFile)
            }
        }
    }
}
