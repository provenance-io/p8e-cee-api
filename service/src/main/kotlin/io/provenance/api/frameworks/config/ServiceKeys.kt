package io.provenance.api.frameworks.config

import javax.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@ConstructorBinding
@ConfigurationProperties(prefix = "service-keys")
@Validated
class ServiceKeys : LoggableProperties() {

    @NotNull
    lateinit var portfolioManager: String
}
