package io.provenance.api.frameworks.web.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader
import org.springframework.http.codec.multipart.MultipartHttpMessageReader
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFlux
class WebConfig(
    private val objectMapper: ObjectMapper,
) : WebFluxConfigurer {

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        // Enable support for serializing protos to JSON in API responses
        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
        configurer.defaultCodecs().maxInMemorySize(500 * 1024 * 1024)

        val partReader = DefaultPartHttpMessageReader()
        partReader.setMaxHeadersSize(16 * 1024 * 1024)
        val multipartReader = MultipartHttpMessageReader(partReader)
        configurer.defaultCodecs().multipartReader(multipartReader)
    }
}
