package io.provenance.api.frameworks.web.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
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
    /*
     * The maximum number of bytes of a multipart part that will be buffered in memory before the
     * reader spills the remainder of that part to a temporary file on disk. Configurable via the
     * MULTIPART_MAX_IN_MEMORY_SIZE environment variable (Spring relaxed binding of the
     * multipart.max-in-memory-size property); when unset it falls back to Spring's default of
     * 262144 bytes (256 KiB).
     */
    @Value("\${multipart.max-in-memory-size:262144}")
    private val multipartMaxInMemorySize: Int,
) : WebFluxConfigurer {

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        // Enable support for serializing protos to JSON in API responses
        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
        configurer.defaultCodecs().maxInMemorySize(500 * 1024 * 1024)

        val partReader = DefaultPartHttpMessageReader()
        partReader.setMaxHeadersSize(16 * 1024 * 1024)
        partReader.setMaxInMemorySize(multipartMaxInMemorySize)
        val multipartReader = MultipartHttpMessageReader(partReader)
        configurer.defaultCodecs().multipartReader(multipartReader)
    }
}
