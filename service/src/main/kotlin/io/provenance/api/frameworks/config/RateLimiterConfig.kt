package io.provenance.api.frameworks.config

import com.google.common.util.concurrent.RateLimiter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RateLimiterConfig {
    @Bean
    fun rateLimiter(
        rateLimiterProps: RateLimiterProps
    ) = RateLimiter.create(rateLimiterProps.permitsPerSecond)
}
