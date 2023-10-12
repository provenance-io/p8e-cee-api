package io.provenance.api.frameworks.config

import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "vault")
class VaultProperties : LoggableProperties() {
    @NotNull
    lateinit var address: String

    @NotNull
    lateinit var tokenPath: String
}
