package io.provenance.onboarding.frameworks.web.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApi {
    @Bean
    fun customOpemApi(): OpenAPI {
        return OpenAPI().components(Components()).info(
            Info()
                .title("p8e-cee-api")
                .description("The p8e-cee-api allows for operations against the encrypted object store, with included support for multi-store replication, and creating and broadcasting scoped transmissions to the Provenance Blockchain.")
        )
    }
}
