package io.provenance.onboarding.frameworks.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.provenance.scope.encryption.domain.inputstream.DIMEInputStream.Companion.configureProvenance
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@EnableConfigurationProperties(
    value = [
        ServiceProps::class,
        ObjectStoreConfig::class,
        VaultProperties::class,
        ProvenanceProperties::class
    ]
)
class AppConfig {
    @Primary
    @Bean
    fun objectMapper() = ObjectMapper()
        .configureProvenance()
}
