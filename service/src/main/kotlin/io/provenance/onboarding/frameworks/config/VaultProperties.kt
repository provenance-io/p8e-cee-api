package io.provenance.onboarding.frameworks.config

import javax.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "vault")
class VaultProperties : LoggableProperties() {
    @NotNull
    lateinit var address: String

    @NotNull
    lateinit var tokenPath: String
}
