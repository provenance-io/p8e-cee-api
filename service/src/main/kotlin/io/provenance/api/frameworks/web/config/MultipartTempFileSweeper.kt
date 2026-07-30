package io.provenance.api.frameworks.web.config

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

/**
 * Periodically deletes stale multipart temporary files that the reactive multipart reader spilled to
 * disk but that were never removed by [org.springframework.http.codec.multipart.FilePart.delete].
 *
 * This is required because Spring WebFlux only runs its own multipart cleanup
 * ([org.springframework.web.server.adapter.HttpWebHandlerAdapter] ->
 * `DefaultServerWebExchange.cleanupMultipart`) once the entire multipart body has been collected and
 * the `multipartRead` flag has been set. When a large upload is aborted (or otherwise errors) part
 * way through parsing, the temp file has already been written to disk — yet neither the framework
 * cleanup nor any application handler ever runs for that part, so the file is orphaned for the whole
 * lifetime of the pod. Left unchecked, this fills the pod's disk with hundreds of GB of
 * `*.multipart` files.
 *
 * The sweeper scans the JVM temp directory ([java.io.tmpdir]) for the reader's
 * `spring-multipart-*` working directories and removes any regular file older than
 * [maxAge]. The age threshold must be comfortably larger than the longest legitimate in-flight
 * upload so that a file currently being written is never deleted out from under an active request.
 */
@Component
class MultipartTempFileSweeper(
    @Value("\${multipart.temp.max-age-ms:7200000}")
    private val maxAgeMs: Long,
    @Value("\${java.io.tmpdir:/tmp}")
    private val tempDirectory: String,
) {
    private val log = KotlinLogging.logger {}

    private val maxAge: Duration = Duration.ofMillis(maxAgeMs)

    /**
     * Deletes stale spilled multipart files. Scheduled with a fixed delay so runs never overlap.
     */
    @Scheduled(
        fixedDelayString = "\${multipart.sweep.interval-ms:1800000}",
        initialDelayString = "\${multipart.sweep.interval-ms:1800000}",
    )
    fun sweep() {
        val root = Path.of(tempDirectory)
        if (!root.isDirectory()) {
            log.warn { "Multipart temp sweep skipped; directory does not exist [tempDirectory=$tempDirectory]" }
            return
        }

        val cutoff = Instant.now().minus(maxAge)
        var deleted = 0L
        var reclaimedBytes = 0L

        runCatching {
            Files.newDirectoryStream(root, "spring-multipart-*").use { multipartDirs ->
                multipartDirs.forEach { dir ->
                    if (dir.isDirectory()) {
                        val (count, bytes) = sweepDirectory(dir, cutoff)
                        deleted += count
                        reclaimedBytes += bytes
                    }
                }
            }
        }.onFailure { error ->
            log.error(error) { "Failed to enumerate multipart temp directories under [tempDirectory=$tempDirectory]" }
        }

        if (deleted > 0) {
            log.info {
                "Multipart temp sweep removed [files=$deleted, reclaimedBytes=$reclaimedBytes] " +
                    "older than [maxAge=$maxAge]"
            }
        }
    }

    private fun sweepDirectory(dir: Path, cutoff: Instant): Pair<Long, Long> {
        var count = 0L
        var bytes = 0L
        runCatching {
            Files.newDirectoryStream(dir).use { entries ->
                entries.forEach { file ->
                    if (file.isStaleMultipartFile(cutoff)) {
                        val size = runCatching { Files.size(file) }.getOrDefault(0L)
                        val removed = runCatching { Files.deleteIfExists(file) }
                            .getOrElse { error ->
                                logDeleteFailure(file, error)
                                false
                            }
                        if (removed) {
                            count++
                            bytes += size
                        }
                    }
                }
            }
        }.onFailure { error ->
            log.error(error) { "Failed to sweep multipart temp directory [dir=${dir.name}]" }
        }
        return count to bytes
    }

    private fun Path.isStaleMultipartFile(cutoff: Instant): Boolean =
        isRegularFile() &&
            name.endsWith(".multipart") &&
            runCatching { getLastModifiedTime().toInstant().isBefore(cutoff) }.getOrDefault(false)

    private fun logDeleteFailure(file: Path, error: Throwable) {
        log.warn(error) { "Failed to delete stale multipart temp file [file=${file.name}]" }
    }
}
