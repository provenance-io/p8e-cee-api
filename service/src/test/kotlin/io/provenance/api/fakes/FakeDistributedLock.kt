package io.provenance.api.fakes

import io.provenance.api.domain.DistributedLock
import java.util.UUID

class FakeDistributedLock : DistributedLock {
    var lockCount = 0
        private set

    override suspend fun <T> withSuspendLock(uuid: UUID, lockedBlock: suspend () -> T): T {
        lockCount++
        return lockedBlock()
    }

    override fun <T> withLock(uuid: UUID, lockedBlock: () -> T): T {
        lockCount++
        return lockedBlock()
    }

    fun resetLockCount() {
        lockCount = 0
    }
}
