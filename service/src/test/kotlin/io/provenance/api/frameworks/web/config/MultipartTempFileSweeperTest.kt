package io.provenance.api.frameworks.web.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import kotlin.io.path.exists

class MultipartTempFileSweeperTest : FunSpec({

    lateinit var tempRoot: Path

    fun ageOf(path: Path, age: java.time.Duration) {
        Files.setLastModifiedTime(path, FileTime.from(Instant.now().minus(age)))
    }

    beforeTest {
        tempRoot = Files.createTempDirectory("sweeper-test-")
    }

    afterTest {
        // Best-effort recursive cleanup of the test scratch directory.
        Files.walk(tempRoot).sorted(Comparator.reverseOrder()).forEach { runCatching { Files.deleteIfExists(it) } }
    }

    test("deletes stale .multipart files inside spring-multipart-* directories") {
        val readerDir = Files.createDirectory(tempRoot.resolve("spring-multipart-1234567890"))
        val stale = Files.createFile(readerDir.resolve("111.multipart"))
        ageOf(stale, java.time.Duration.ofHours(3))

        val sweeper = MultipartTempFileSweeper(
            maxAgeMs = java.time.Duration.ofHours(2).toMillis(),
            tempDirectory = tempRoot.toString(),
        )

        sweeper.sweep()

        stale.exists() shouldBe false
    }

    test("keeps fresh .multipart files younger than max age") {
        val readerDir = Files.createDirectory(tempRoot.resolve("spring-multipart-2222222222"))
        val fresh = Files.createFile(readerDir.resolve("222.multipart"))
        ageOf(fresh, java.time.Duration.ofMinutes(5))

        val sweeper = MultipartTempFileSweeper(
            maxAgeMs = java.time.Duration.ofHours(2).toMillis(),
            tempDirectory = tempRoot.toString(),
        )

        sweeper.sweep()

        fresh.exists() shouldBe true
    }

    test("ignores stale files that are not .multipart") {
        val readerDir = Files.createDirectory(tempRoot.resolve("spring-multipart-3333333333"))
        val other = Files.createFile(readerDir.resolve("notes.txt"))
        ageOf(other, java.time.Duration.ofHours(3))

        val sweeper = MultipartTempFileSweeper(
            maxAgeMs = java.time.Duration.ofHours(2).toMillis(),
            tempDirectory = tempRoot.toString(),
        )

        sweeper.sweep()

        other.exists() shouldBe true
    }

    test("ignores stale .multipart files outside spring-multipart-* directories") {
        val unrelatedDir = Files.createDirectory(tempRoot.resolve("datadog"))
        val stray = Files.createFile(unrelatedDir.resolve("444.multipart"))
        ageOf(stray, java.time.Duration.ofHours(3))

        val sweeper = MultipartTempFileSweeper(
            maxAgeMs = java.time.Duration.ofHours(2).toMillis(),
            tempDirectory = tempRoot.toString(),
        )

        sweeper.sweep()

        stray.exists() shouldBe true
    }

    test("does not fail when temp directory does not exist") {
        val sweeper = MultipartTempFileSweeper(
            maxAgeMs = java.time.Duration.ofHours(2).toMillis(),
            tempDirectory = tempRoot.resolve("does-not-exist").toString(),
        )

        // Should simply log and return without throwing.
        sweeper.sweep()
    }
})
