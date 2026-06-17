package io.provenance.api.util

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono

/**
 * Reads the full contents of this [FilePart] into a [ByteArray].
 *
 * The intermediate [DataBuffer] produced by [DataBufferUtils.join] is always released once its
 * readable bytes have been copied out, including on error or cancellation. Failing to release it
 * leaks pooled/direct buffers and increases heap and native-memory pressure under load.
 *
 * NOTE: callers remain responsible for invoking [FilePart.delete] to remove any temporary file that
 * the reactive multipart reader may have spilled to disk for this part.
 */
fun FilePart.awaitAllBytes(): Mono<ByteArray> =
    DataBufferUtils.join(this.content()).map { dataBuffer ->
        try {
            ByteArray(dataBuffer.readableByteCount()).also { bytes ->
                dataBuffer.read(bytes)
            }
        } finally {
            DataBufferUtils.release(dataBuffer)
        }
    }
