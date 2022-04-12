package io.provenance.onboarding.frameworks.config

import javax.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "service-keys")
class ServiceKeysProperties : LoggableProperties() {

    @NotNull
    lateinit var portfolioManager: String

    @NotNull
    lateinit var dart: String
}
