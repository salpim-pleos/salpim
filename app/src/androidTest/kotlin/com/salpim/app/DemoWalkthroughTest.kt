package com.salpim.app

import android.os.SystemClock
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.salpim.core.model.Gear
import com.salpim.core.signal.sim.Scenarios
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 시연 영상 녹화용 흐름. 검증이 아니라 사람이 읽을 수 있게 천천히 화면을 넘긴다.
 * 평소 e2e(scripts/e2e.sh)에서는 건너뛰고, 계측 인자 salpimDemo=true일 때만 실행한다.
 *   adb shell am instrument -w -e salpimDemo true -e class com.salpim.app.DemoWalkthroughTest \
 *     com.salpim.app.test/androidx.test.runner.AndroidJUnitRunner
 * 신호는 모두 SimulatedSignalSource 시나리오이며, 화면의 "시뮬레이션 신호" 표시는 그대로 둔다.
 */
@RunWith(AndroidJUnit4::class)
class DemoWalkthroughTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val container get() = (rule.activity.application as SalpimApp).container
    private val sim get() = checkNotNull(container.simulated) { "시연 영상은 시뮬레이션 모드에서 녹화한다" }

    @Before
    fun setUp() {
        val demoRequested = InstrumentationRegistry.getArguments().getString(DEMO_ARGUMENT) == "true"
        assumeTrue("시연 녹화용: -e $DEMO_ARGUMENT true 일 때만 실행", demoRequested)
        rule.runOnUiThread { container.resetForTest() }
        waitForTag("screen_stopped")
    }

    @Test
    fun walkthrough() {
        pause(READ_LONG_MS) // 주차: 내 차 상태
        drivingWithEngineOilWarning()
        parkingThenServiceByVoice()
        lowFuelTrip()
    }

    private fun drivingWithEngineOilWarning() {
        rule.onNodeWithTag("scenario_engine_oil_warning").performScrollTo().performClick()
        waitForTag("screen_driving", SCENARIO_STEP_TIMEOUT_MS)
        waitForTag("warning_banner", SCENARIO_STEP_TIMEOUT_MS)
        pause(READ_LONG_MS)
    }

    private fun parkingThenServiceByVoice() {
        // 주행 중에는 시나리오 버튼이 없으므로(안전 규칙) 이어지는 시나리오는 신호원에서 바로 재생한다
        rule.runOnUiThread { sim.play(Scenarios.PARKING_AFTER_WARNING) }
        waitForTag("screen_stopped", SCENARIO_STEP_TIMEOUT_MS)
        waitForTag("warning_detail")
        pause(READ_LONG_MS) // 지금 할 일

        rule.onNodeWithText("가까운 블루핸즈 찾아 줘").performScrollTo().performClick()
        waitForTag("places_list")
        waitForText("곳을 찾았어요")
        rule.onNodeWithTag("places_list").performScrollTo()
        pause(READ_MS)

        rule.onNodeWithText("첫 번째로 가 줘").performScrollTo().performClick()
        waitForTag("nav_message")
        rule.onNodeWithTag("nav_message").performScrollTo()
        pause(READ_MS)

        rule.onNodeWithTag("btn_reservation").performScrollTo().performClick()
        waitForTag("reservation_panel")
        rule.onNodeWithTag("reservation_panel").performScrollTo()
        pause(READ_LONG_MS)
    }

    private fun lowFuelTrip() {
        rule.onNodeWithTag("scenario_low_fuel_trip").performScrollTo().performClick()
        waitForTag("screen_driving", SCENARIO_STEP_TIMEOUT_MS)
        pause(LOW_FUEL_DRIVE_MS) // 주행 중 연료 한 줄
        rule.runOnUiThread { sim.set { copy(speedKmh = 0f, gear = Gear.PARK, parkingBrakeOn = true) } }
        waitForTag("screen_stopped", SCENARIO_STEP_TIMEOUT_MS)
        waitForTag("btn_find_gas")
        rule.onNodeWithTag("btn_find_gas").performScrollTo()
        pause(READ_LONG_MS) // 정차 후 주유 안내
    }

    private fun waitForTag(tag: String, timeoutMs: Long = STEP_TIMEOUT_MS) =
        rule.waitUntil(timeoutMs) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }

    private fun waitForText(text: String) = rule.waitUntil(STEP_TIMEOUT_MS) {
        rule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    }

    /** 영상 시청자가 읽을 시간. 동기화 목적이 아니므로 조건 없이 시간만 흘려보낸다 (화면은 계속 갱신) */
    private fun pause(durationMs: Long) {
        val endMs = SystemClock.uptimeMillis() + durationMs
        rule.waitUntil(durationMs + PAUSE_SLACK_MS) { SystemClock.uptimeMillis() >= endMs }
    }

    private companion object {
        const val DEMO_ARGUMENT = "salpimDemo"
        const val READ_MS = 4_000L
        const val READ_LONG_MS = 6_000L
        const val LOW_FUEL_DRIVE_MS = 12_000L
        const val STEP_TIMEOUT_MS = 10_000L
        const val SCENARIO_STEP_TIMEOUT_MS = 40_000L
        const val PAUSE_SLACK_MS = 5_000L
    }
}
