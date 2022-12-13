package io.provenance.api.util

import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono

fun FilePart.awaitAllBytes(): Mono<ByteArray> =
    DataBufferUtils.join(this.content()).map { it.asByteBuffer().array() }
