package com.figure.onboarding.frameworks.web.external.objectStore

import com.figure.onboarding.frameworks.web.Routes
import com.figure.onboarding.frameworks.web.logging.logExchange
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter

private val log = KotlinLogging.logger {}

@Configuration
class ObjectStoreApi {
    @Bean
    fun externalObjectStoreApiV1(handler: ObjectStoreHandler) = coRouter {
        logExchange(log)
        Routes.EXTERNAL_BASE_V1.nest {
            "/eos".nest {
                POST("", handler::store)
                POST("/snapshot", handler::snapshot)
                GET("", handler::getAsset)
            }
            POST("/config/replication/enable", handler::enableReplication)
        }
    }
}
