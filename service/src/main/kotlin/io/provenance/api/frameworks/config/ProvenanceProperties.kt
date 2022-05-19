package io.provenance.api.frameworks.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "p8e")
@Validated
class ProvenanceProperties {
    var mainnet: Boolean = false
}
