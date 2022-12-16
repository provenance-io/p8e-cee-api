package io.provenance.api.frameworks.web.internal.objectStore

import io.provenance.api.frameworks.web.Routes
import io.provenance.api.frameworks.web.logging.logExchange
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter

private val log = KotlinLogging.logger {}

@Configuration
class InternalObjectStoreApi {
    @Bean
    fun internalObjectStoreApiV1(handler: InternalObjectStoreHandler) = coRouter {
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
