package io.provenance.api.frameworks.config

import org.springframework.boot.context.properties.ConfigurationProperties
import javax.validation.constraints.NotNull

@ConfigurationProperties(prefix = "service-keys")
class ServiceKeysProperties : LoggableProperties() {

    @NotNull
    lateinit var portfolioManager: String

    @NotNull
    lateinit var dart: String
}
