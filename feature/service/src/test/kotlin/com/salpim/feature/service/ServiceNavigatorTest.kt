package com.salpim.feature.service

import com.salpim.core.model.NavResult
import com.salpim.core.model.NavigationLauncher
import com.salpim.core.model.ServiceDestination
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceNavigatorTest {
    private class FakeLauncher(override val isAvailable: Boolean, val result: NavResult) : NavigationLauncher {
        var last: ServiceDestination? = null
        override suspend fun navigateTo(destination: ServiceDestination): NavResult {
            last = destination
            return result
        }

        override suspend fun openSearch(keyword: String): NavResult = result
    }

    private val shop = ServiceDestination("동네 정비소", 37.5, 127.0)

    @Test
    fun asksForFavoriteWhenMissing() = runTest {
        val msg = ServiceNavigator(FakeLauncher(true, NavResult.Started), FavoriteShopStore()).navigateToFavorite()
        assertTrue(msg.contains("지정"))
    }

    @Test
    fun startsNavigationToFavorite() = runTest {
        val launcher = FakeLauncher(true, NavResult.Started)
        val msg = ServiceNavigator(launcher, FavoriteShopStore(shop)).navigateToFavorite()
        assertEquals(shop, launcher.last)
        assertTrue(msg.contains("동네 정비소"))
    }

    @Test
    fun unavailableLauncherIsNotCalled() = runTest {
        val launcher = FakeLauncher(false, NavResult.Started)
        val msg = ServiceNavigator(launcher, FavoriteShopStore(shop)).navigateToFavorite()
        assertEquals(null, launcher.last)
        assertTrue(msg.contains("준비"))
    }
}
