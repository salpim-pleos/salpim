package com.salpim.app

import android.os.SystemClock
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 시연 영상 녹화용 대화 흐름. 검증이 아니라 사람이 읽을 수 있게 천천히 넘긴다.
 * 평소 e2e(scripts/e2e.sh)에서는 건너뛰고, 계측 인자 salpimDemo=true일 때만 실행한다.
 *   adb shell am instrument -w -e salpimDemo true -e class com.salpim.app.DemoWalkthroughTest \
 *     com.salpim.app.test/androidx.test.runner.AndroidJUnitRunner
 *
 * 운전자의 말은 음성 인식 결과와 같은 경로(onHint = 들은 문장)로 넣는다. 에뮬레이터에는 마이크 입력이 없기 때문이다.
 * 신호는 모두 SimulatedSignalSource 시나리오이며, 화면의 "시뮬레이션 신호" 표시는 그대로 둔다.
 */
@RunWith(AndroidJUnit4::class)
class DemoWalkthroughTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val container get() = (rule.activity.application as SalpimApp).container
    private val vm get() = ViewModelProvider(rule.activity)[MainViewModel::class.java]

    @Before
    fun setUp() {
        val demoRequested = InstrumentationRegistry.getArguments().getString(DEMO_ARGUMENT) == "true"
        assumeTrue("시연 녹화용: -e $DEMO_ARGUMENT true 일 때만 실행", demoRequested)
        rule.runOnUiThread { container.resetForTest() }
        waitForTag("screen_stopped")
    }

    @Test
    fun walkthrough() {
        pause(READ_MS) // 주차: 내 차 상태
        departToAirport()
        refuelOnTheWay()
        warningWhileDriving()
        serviceAfterStopping()
    }

    /** "인천공항 가 줘" → 출발, 내비 그림이 움직이고 경로가 잡힌다 */
    private fun departToAirport() {
        say("인천공항 가 줘")
        waitForTag("screen_driving", SCENARIO_STEP_TIMEOUT_MS)
        pause(READ_MS)
    }

    /** 경로가 잡히면 연료가 모자라다고 먼저 알리고, 가장 싼 주유소를 권한 뒤 경유지로 */
    private fun refuelOnTheWay() {
        waitForText("주유소를 찾아 드릴까요", SCENARIO_STEP_TIMEOUT_MS)
        pause(READ_MS)
        say("어디서 넣어?")
        waitForText("가장 싸요")
        pause(READ_LONG_MS)
        say("응")
        waitForText("안내할게요")
        pause(READ_MS)
    }

    /** 가는 길에 엔진오일 경고 → "계속 달려도 돼?" */
    private fun warningWhileDriving() {
        waitForText("엔진오일이 부족해요", SCENARIO_STEP_TIMEOUT_MS)
        pause(READ_MS)
        say("계속 달려도 돼?")
        waitForText("당장 멈출 필요는 없지만")
        pause(READ_MS)
    }

    /** 주유소에 세우면 할 일 → 블루핸즈 찾기 → 길 안내 → 휴대폰 예약 QR */
    private fun serviceAfterStopping() {
        waitForTag("screen_stopped", SCENARIO_STEP_TIMEOUT_MS)
        waitForTag("warning_detail")
        pause(READ_LONG_MS)
        say("가까운 블루핸즈 찾아 줘")
        waitForTag("places_list")
        waitForText("곳을 찾았어요")
        pause(READ_MS)
        say("첫 번째로 가 줘")
        waitForTag("nav_message")
        pause(READ_MS)
        say("예약할래")
        waitForTag("reservation_panel")
        pause(READ_LONG_MS)
    }

    private fun say(text: String) = rule.runOnUiThread { vm.onHint(text) }

    private fun waitForTag(tag: String, timeoutMs: Long = STEP_TIMEOUT_MS) =
        rule.waitUntil(timeoutMs) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }

    private fun waitForText(text: String, timeoutMs: Long = STEP_TIMEOUT_MS) = rule.waitUntil(timeoutMs) {
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
        const val STEP_TIMEOUT_MS = 10_000L
        const val SCENARIO_STEP_TIMEOUT_MS = 60_000L
        const val PAUSE_SLACK_MS = 5_000L
    }
}
