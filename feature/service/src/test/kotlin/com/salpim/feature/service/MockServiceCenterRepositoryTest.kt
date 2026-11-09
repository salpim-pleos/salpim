package com.salpim.feature.service

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MockServiceCenterRepositoryTest {
    private val repo = MockServiceCenterRepository.fromJson(
        File("src/main/assets/${MockServiceCenterRepository.ASSET_NAME}").readText(Charsets.UTF_8),
    )

    @Test
    fun mockIsFlaggedAndSortedByDistance() = runTest {
        assertTrue(repo.isMock)
        val list = repo.nearby()
        assertTrue(list.isNotEmpty())
        assertEquals(list.sortedBy { it.distanceKm }, list)
    }

    @Test
    fun mockEntriesAreClearlyLabeledAsExamples() = runTest {
        repo.nearby(limit = 100).forEach { assertTrue(it.name, it.name.contains("예시")) }
    }

    @Test
    fun limitIsRespected() = runTest {
        assertEquals(1, repo.nearby(limit = 1).size)
    }

    @Test
    fun centerConvertsToDestination() = runTest {
        val c = repo.nearby().first()
        val d = c.toDestination()
        assertEquals(c.name, d.name)
        assertEquals(c.latitude, d.latitude, 0.0)
    }
}
