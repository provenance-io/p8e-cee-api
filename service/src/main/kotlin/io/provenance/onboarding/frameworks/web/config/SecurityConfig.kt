package io.provenance.onboarding.frameworks.web.config

import io.provenance.onboarding.frameworks.config.ServiceProps
import io.provenance.onboarding.frameworks.web.Routes
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(value = [ServiceProps::class])
class SecurityConfig {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.authorizeExchange {
            it.pathMatchers("${Routes.MANAGE_BASE}/**", "${Routes.EXTERNAL_BASE}/**", "${Routes.INTERNAL_BASE}/**", "${Routes.DOCS_BASE}/**").permitAll()
        }.csrf().disable()

        return http.build()
    }
}
