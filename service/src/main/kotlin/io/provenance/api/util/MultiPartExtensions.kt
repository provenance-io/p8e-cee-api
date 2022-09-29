package io.provenance.api.util

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart

suspend fun FilePart.awaitAllBytes(): ByteArray =
    DataBufferUtils.join(this.content()).map { it.asByteBuffer().array() }.awaitSingle()
