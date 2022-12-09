package io.provenance.api.frameworks.config

import kotlin.properties.Delegates
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "limiter")
@Validated
class RateLimiterProps {
    var permitsPerSecond by Delegates.notNull<Double>()
    var permitsToAcquire by Delegates.notNull<Int>()
    var backoffSeconds by Delegates.notNull<Long>()
}
