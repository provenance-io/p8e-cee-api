package io.provenance.api.domain.extensions

import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.InvalidMediaTypeException
import org.springframework.http.MediaType

@Suppress("SwallowedException")
fun ByteArray.toByteResponse(
    filename: String,
    contentType: String
) = let { bytes ->
    bytes to HttpHeaders().also {
        it.contentDisposition = ContentDisposition.builder("inline").filename(filename).build()
        it.contentLength = bytes.size.toLong()

        contentType.run {
            try {
                MediaType.parseMediaType(contentType)
            } catch (e: InvalidMediaTypeException) {
                // ignored
                MediaType.APPLICATION_OCTET_STREAM
            }
        }.run { it.contentType = this }
    }
}.let { (bytes, headers) -> HttpEntity(bytes, headers) }
