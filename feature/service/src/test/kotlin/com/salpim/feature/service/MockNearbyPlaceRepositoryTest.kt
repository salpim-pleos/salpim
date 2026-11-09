package com.salpim.feature.service

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MockNearbyPlaceRepositoryTest {
    private val repo = MockNearbyPlaceRepository.fromJson(
        File("src/main/assets/${MockNearbyPlaceRepository.ASSET_NAME}").readText(Charsets.UTF_8),
    )

    @Test
    fun mockIsFlaggedAndSortedByDistance() = runTest {
        assertTrue(repo.isMock)
        val list = repo.nearby(PlaceCategory.BLUE_HANDS)
        assertTrue(list.isNotEmpty())
        assertEquals(list.sortedBy { it.distanceKm }, list)
    }

    @Test
    fun categoriesDoNotMix() = runTest {
        assertTrue(repo.nearby(PlaceCategory.GAS_STATION).all { it.category == PlaceCategory.GAS_STATION })
        assertTrue(repo.nearby(PlaceCategory.BLUE_HANDS).all { it.category == PlaceCategory.BLUE_HANDS })
    }

    @Test
    fun mockEntriesAreClearlyLabeledAsExamples() = runTest {
        PlaceCategory.entries.forEach { category ->
            repo.nearby(category, limit = 100).forEach { assertTrue(it.name, it.name.contains("예시")) }
        }
    }

    @Test
    fun limitIsRespected() = runTest {
        assertEquals(1, repo.nearby(PlaceCategory.GAS_STATION, limit = 1).size)
    }

    @Test
    fun placeConvertsToDestination() = runTest {
        val place = repo.nearby(PlaceCategory.BLUE_HANDS).first()
        val destination = place.toDestination()
        assertEquals(place.name, destination.name)
        assertEquals(place.latitude, destination.latitude, 0.0)
    }
}
