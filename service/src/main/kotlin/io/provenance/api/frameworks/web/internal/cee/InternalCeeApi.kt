package io.provenance.api.frameworks.web.internal.cee

import io.provenance.api.frameworks.web.Routes
import io.provenance.api.frameworks.web.logging.logExchange
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter

private val log = KotlinLogging.logger {}

@Configuration
class InternalCeeApi {
    @Bean
    fun internalCeeApiV1(handler: InternalCeeHandler) = coRouter {
        logExchange(log)
        "${Routes.INTERNAL_BASE_V1}/cee".nest {
            POST("/approve", handler::approveContractExecution)
            POST("/execute", handler::executeContract)
            POST("/submit", handler::submitContractResult)
            POST("/reject", handler::rejectContractExecution)
            GET("/headers", handler::showHeaders)
        }
    }
}
