package io.provenance.api.domain.usecase.objectstore

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.objectStore.store.StoreFile
import io.provenance.api.domain.usecase.objectStore.store.StoreObject
import io.provenance.api.domain.usecase.objectStore.store.models.StoreFileRequestWrapper
import io.provenance.api.models.entity.MemberUUID
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.util.LinkedMultiValueMap
import reactor.core.publisher.Mono
import java.util.UUID

class StoreFileCleanupTest : FunSpec({

    val entityManager = mockk<EntityManager>()
    val storeObject = mockk<StoreObject>(relaxed = true)
    val storeFile = StoreFile(entityManager, storeObject)

    fun mockFilePart(): FilePart = mockk<FilePart>(relaxed = true).also {
        every { it.delete() } returns Mono.empty()
    }

    test("deletes every FilePart including duplicates under the same field name on a failed request") {
        val firstFile = mockFilePart()
        val duplicateFile = mockFilePart()

        // Two parts share the "file" field name; toSingleValueMap() would have dropped the second.
        val request = LinkedMultiValueMap<String, Part>().apply {
            add("file", firstFile)
            add("file", duplicateFile)
        }

        // No "id" field -> getParams() throws before any store happens, exercising the finally block.
        shouldThrow<IllegalArgumentException> {
            storeFile.execute(StoreFileRequestWrapper(MemberUUID(UUID.randomUUID()), request))
        }

        verify(exactly = 1) { firstFile.delete() }
        verify(exactly = 1) { duplicateFile.delete() }
    }

    test("deletes FileParts spread across multiple field names") {
        val fileA = mockFilePart()
        val fileB = mockFilePart()

        val request = LinkedMultiValueMap<String, Part>().apply {
            add("file", fileA)
            add("attachment", fileB)
        }

        shouldThrow<IllegalArgumentException> {
            storeFile.execute(StoreFileRequestWrapper(MemberUUID(UUID.randomUUID()), request))
        }

        verify(exactly = 1) { fileA.delete() }
        verify(exactly = 1) { fileB.delete() }
    }
})
