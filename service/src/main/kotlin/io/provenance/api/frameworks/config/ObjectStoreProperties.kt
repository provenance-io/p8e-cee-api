package io.provenance.api.frameworks.config

import kotlin.properties.Delegates
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "object-store")
@Validated
class ObjectStoreProperties : LoggableProperties() {
    var timeoutMs by Delegates.notNull<Long>()
}
