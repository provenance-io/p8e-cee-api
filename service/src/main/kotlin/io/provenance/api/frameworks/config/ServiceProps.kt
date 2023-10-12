package io.provenance.api.frameworks.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "service")
@Validated
data class ServiceProps(
    val name: String,
    val environment: String,
) {

    fun isProd() = environment == "production"

    override fun toString(): String {
        return """Service Properties:
            | name: $name
            | environment: $environment
        """.trimMargin()
    }
}
