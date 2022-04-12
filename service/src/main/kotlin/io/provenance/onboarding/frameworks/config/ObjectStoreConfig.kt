package io.provenance.onboarding.frameworks.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import kotlin.properties.Delegates

@ConfigurationProperties(prefix = "object-store")
@Validated
class ObjectStoreConfig : LoggableProperties() {
    var timeoutMs by Delegates.notNull<Long>()
}
