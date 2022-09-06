package io.provenance.api.frameworks.web.internal.provenance

import io.provenance.api.frameworks.web.Routes
import io.provenance.api.frameworks.web.logging.logExchange
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter

private val log = KotlinLogging.logger {}

@Configuration
class InternalProvenanceApi {
    @Bean
    fun internalProvenanceApiV1(handler: InternalProvenanceHandler) = coRouter {
        logExchange(log)
        "${Routes.INTERNAL_BASE_V1}/p8e".nest {
            "/classify".nest {
                POST("", handler::classifyAsset)
                GET("/status", handler::getClassificationStatus)
                GET("/fees", handler::getFees)
                GET("/all", handler::getAssetDefinitions)
            }
        }
    }
}
