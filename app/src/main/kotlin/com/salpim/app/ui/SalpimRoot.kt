package com.salpim.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.salpim.app.MainUiState
import com.salpim.core.designsystem.Exchange
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.designsystem.SimulationBadge
import com.salpim.core.designsystem.SimulationItem
import com.salpim.core.model.Urgency

/** UxPolicy가 제한이면 주행 화면, 아니면 정차 화면 */
@Composable
fun SalpimRoot(ui: MainUiState, actions: MainActions) {
    if (ui.policy.restricted) DrivingScreen(ui, actions) else StoppedScreen(ui, actions)
}

/**
 * 시뮬레이션이면 음성 바 오른쪽 끝에 항상 표시 (AGENTS.md 2절). 정차 중에만 눌러서 시연 시나리오를 고른다.
 */
@Composable
internal fun SimulationMenu(ui: MainUiState, actions: MainActions, enabled: Boolean) {
    if (!ui.simulated) return
    SimulationBadge(
        items = ui.scenarios.map { SimulationItem(it.id, it.title, playing = it.id == ui.playingScenario) },
        menuEnabled = enabled,
        onSelect = actions::playScenario,
    )
}

/** 음성 바에 보여 줄 마지막 주고받음 */
internal fun MainUiState.exchange(): Exchange? = inputs.lastReply?.let { Exchange(inputs.lastHeard, it) }

/** 긴급도 → 상태 색 (평상시엔 색을 쓰지 않는다) */
fun Urgency.color(): Color = when (this) {
    Urgency.IMMEDIATE_STOP -> SalpimColors.Urgent
    Urgency.SERVICE_SOON -> SalpimColors.Caution
    Urgency.INFO -> SalpimColors.Notice
}

fun Urgency.label(): String = when (this) {
    Urgency.IMMEDIATE_STOP -> "즉시 정차"
    Urgency.SERVICE_SOON -> "점검 권장"
    Urgency.INFO -> "안내"
}
