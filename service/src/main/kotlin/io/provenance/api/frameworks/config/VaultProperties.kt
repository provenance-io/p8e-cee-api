package io.provenance.api.frameworks.config

import org.springframework.boot.context.properties.ConfigurationProperties
import javax.validation.constraints.NotNull

@ConfigurationProperties(prefix = "vault")
class VaultProperties : LoggableProperties() {
    @NotNull
    lateinit var address: String

    @NotNull
    lateinit var tokenPath: String
}
