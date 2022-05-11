package io.provenance.onboarding.frameworks.web.internal.objectStore

import io.provenance.onboarding.frameworks.web.Routes
import io.provenance.onboarding.frameworks.web.logging.logExchange
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
                POST("", handler::store)
            }
        }
    }
}
