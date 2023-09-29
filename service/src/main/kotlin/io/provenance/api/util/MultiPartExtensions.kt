package io.provenance.api.util

import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono
import java.nio.ByteBuffer

fun FilePart.awaitAllBytes(): Mono<ByteArray> =
    DataBufferUtils.join(this.content()).map { dataBuffer ->
        ByteBuffer.allocate(dataBuffer.capacity()).also { byteBuffer ->
            dataBuffer.toByteBuffer(byteBuffer)
        }.array()
    }
