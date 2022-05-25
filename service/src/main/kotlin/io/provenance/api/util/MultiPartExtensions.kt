package io.provenance.api.util

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.codec.multipart.FilePart
import java.io.ByteArrayOutputStream

suspend fun FilePart.awaitAllBytes(): ByteArray = ByteArrayOutputStream().use { stream ->
    /* read all data frames from the request, then concat together via the output stream */
    content().collectList().awaitSingle().forEach { buffer ->
        stream.writeBytes(buffer.asByteBuffer().array())
    }
    stream.toByteArray()
}
