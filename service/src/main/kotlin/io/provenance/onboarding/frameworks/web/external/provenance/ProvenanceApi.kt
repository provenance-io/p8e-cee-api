package io.provenance.onboarding.frameworks.web.external.provenance

import io.provenance.onboarding.frameworks.web.Routes
import io.provenance.onboarding.frameworks.web.logging.logExchange
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter

private val log = KotlinLogging.logger {}

@Configuration
class ProvenanceApi {
    @Bean
    fun externalProvenanceApiV1(handler: ProvenanceHandler) = coRouter {
        logExchange(log)
        "${Routes.EXTERNAL_BASE_V1}/p8e".nest {
            POST("/onboard", handler::createTxAndOnboard)
            POST("/specifications", handler::writeSpecifications)
            POST("/tx/generate", handler::generateTx)
            POST("/tx/execute", handler::executeTx)
        }
    }
}
