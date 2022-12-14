package io.provenance.api.frameworks.web.internal.objectStore

import com.google.common.util.concurrent.RateLimiter
import io.provenance.api.frameworks.config.RateLimiterProps
import io.provenance.api.frameworks.web.Routes
import io.provenance.api.frameworks.web.logging.logExchange
import io.provenance.api.frameworks.web.misc.rateLimitedCoRouter
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val log = KotlinLogging.logger {}

@Configuration
class InternalObjectStoreApi(
    private val rateLimiter: RateLimiter,
    private val rateLimiterProps: RateLimiterProps
) {
    @Bean
    fun internalObjectStoreApiV1(handler: InternalObjectStoreHandler) = rateLimitedCoRouter(rateLimiter, rateLimiterProps) {
        logExchange(log)
        Routes.INTERNAL_BASE_V1.nest {
            "/eos".nest {
                POST("", handler::storeProto)
                GET("", handler::getProto)
                POST("/file", handler::storeFile)
                GET("/file", handler::getFile)
            }
        }

        Routes.INTERNAL_BASE_V2.nest {
            "/eos".nest {
                POST("", handler::storeProtoV2)
                GET("", handler::getProtoV2)
                POST("/file", handler::storeFileV2)
                GET("/file", handler::getFileV2)
            }
        }
    }
}
