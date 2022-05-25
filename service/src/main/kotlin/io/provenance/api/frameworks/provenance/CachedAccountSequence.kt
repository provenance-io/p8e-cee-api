package io.provenance.api.frameworks.provenance

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class CachedAccountSequence {
    private val lastKnownSequence = AtomicLong(0L)
    private val offset = AtomicInteger(0)

    fun getAndIncrementOffset(currentSequence: Long): Int {
        if (lastKnownSequence.get() != currentSequence || offset.get() > 5) {
            lastKnownSequence.set(currentSequence)
            offset.set(0)
        }
        return offset.getAndIncrement()
    }

    fun getAndDecrement(usedSequence: Long) {
        if (lastKnownSequence.get() == usedSequence && offset.get() != 0) {
            offset.getAndDecrement()
        }
    }
}
