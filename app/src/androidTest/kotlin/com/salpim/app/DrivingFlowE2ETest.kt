package com.salpim.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.salpim.core.model.Gear
import com.salpim.core.model.UxRestrictionFlags
import com.salpim.core.model.VehicleSignal
import com.salpim.core.model.WarningCodes
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 화면 단 e2e. SimulatedSignalSource로 신호를 넣고 실제 Activity/Compose 화면을 검증한다.
 * 실행: scripts/e2e.sh (연결된 에뮬레이터 또는 Gradle Managed Device)
 */
@RunWith(AndroidJUnit4::class)
class DrivingFlowE2ETest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val container get() = (rule.activity.application as SalpimApp).container
    private val sim get() = checkNotNull(container.simulated) { "e2e는 시뮬레이션 모드에서 실행한다" }

    @Before
    fun reset() {
        rule.runOnUiThread { container.resetForTest() }
        waitForTag("screen_stopped")
    }

    private fun setSignal(f: VehicleSignal.() -> VehicleSignal) = rule.runOnUiThread { sim.set(f) }

    private fun waitForTag(tag: String, timeoutMs: Long = 5_000) =
        rule.waitUntil(timeoutMs) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }

    private fun driveWithTireWarning() = setSignal {
        copy(gear = Gear.DRIVE, parkingBrakeOn = false, speedKmh = 60f, activeWarnings = setOf(WarningCodes.TIRE_PRESSURE_LOW))
    }

    @Test
    fun simulatedBadgeIsAlwaysVisible() {
        rule.onNodeWithTag("badge_simulated").assertIsDisplayed()
        setSignal { copy(gear = Gear.DRIVE, parkingBrakeOn = false, speedKmh = 40f) }
        waitForTag("screen_driving")
        rule.onNodeWithTag("badge_simulated").assertIsDisplayed()
    }

    @Test
    fun drivingShowsOneLineWarningWithoutDetailOrButtons() {
        driveWithTireWarning()
        waitForTag("screen_driving")
        waitForTag("warning_banner")
        rule.onNodeWithText("타이어 공기압이 낮아요", substring = true).assertIsDisplayed()
        rule.onAllNodesWithTag("warning_detail").assertCountEquals(0)
        rule.onAllNodesWithTag("btn_navigate_service").assertCountEquals(0)
        rule.onAllNodesWithTag("scenario_tire_warning").assertCountEquals(0)
        // 배너 + 글랜스 2개 = 최대 3개
        rule.onAllNodesWithTag("glance_warning").assertCountEquals(0)
        rule.onNodeWithTag("glance_energy").assertIsDisplayed()
        rule.onNodeWithTag("glance_score").assertIsDisplayed()
    }

    @Test
    fun afterParkingDetailAndServiceNavigationAreAvailable() {
        driveWithTireWarning()
        waitForTag("screen_driving")
        setSignal { copy(speedKmh = 0f, gear = Gear.PARK, parkingBrakeOn = true) }
        // 디바운스(1.5초) 후 정차 화면
        waitForTag("screen_stopped")
        rule.onNodeWithTag("warning_item_TIRE_PRESSURE_LOW").performScrollTo().performClick()
        rule.onNodeWithTag("warning_detail").assertExists()
        rule.onNodeWithTag("btn_navigate_service").performScrollTo().performClick()
        // 블루핸즈 목록(mock) → 첫 지점 길 안내
        waitForTag("bluehands_list")
        rule.onNodeWithTag("badge_mock").assertExists()
        rule.onNodeWithTag("btn_route_mock-a").performScrollTo().performClick()
        waitForTag("nav_message")
    }

    @Test
    fun scenarioButtonDrivesTheApp() {
        rule.onNodeWithTag("scenario_tire_warning").performScrollTo().performClick()
        waitForTag("screen_driving", timeoutMs = 10_000)
        waitForTag("warning_banner", timeoutMs = 20_000)
    }

    @Test
    fun systemUxRestrictionForcesDrivingLayoutWhileParked() {
        val ux = container.staticUx
        assumeTrue("자동차 기기에서는 실제 CarUxRestrictions를 쓰므로 건너뜀", ux != null)
        rule.runOnUiThread { ux!!.set(UxRestrictionFlags(requiresDistractionOptimization = true)) }
        waitForTag("screen_driving")
        rule.runOnUiThread { ux!!.set(UxRestrictionFlags.NONE) }
        waitForTag("screen_stopped")
    }
}
