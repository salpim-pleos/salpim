package com.salpim.core.data

import com.salpim.core.model.DrivingEvent
import com.salpim.core.model.DrivingEventType
import com.salpim.core.model.WarningCodes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryHistoryRepositoryTest {
    @Test
    fun sameWarningIsRecordedOnce() {
        val repo = InMemoryHistoryRepository()
        repo.recordWarning(WarningCodes.AIRBAG_DEPLOYED, 1)
        repo.recordWarning(WarningCodes.AIRBAG_DEPLOYED, 2)
        assertEquals(1, repo.warnings.value.size)
        assertEquals(1L, repo.warnings.value.single().firstSeenMs)
    }

    @Test
    fun clearAllRemovesEverything() {
        val repo = InMemoryHistoryRepository()
        repo.recordWarning(WarningCodes.AIRBAG_DEPLOYED, 1)
        repo.recordEvent(DrivingEvent(DrivingEventType.HARD_BRAKE, 1, 20f))
        repo.clearAll()
        assertTrue(repo.warnings.value.isEmpty())
        assertTrue(repo.events.value.isEmpty())
    }
}
