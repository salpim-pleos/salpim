package com.salpim.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.salpim.app.MainUiState
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.designsystem.SimulationLabel
import com.salpim.core.model.Urgency

/** UxPolicy가 제한이면 주행 화면, 아니면 정차 화면. 시뮬레이션이면 항상 표시 */
@Composable
fun SalpimRoot(ui: MainUiState, actions: MainActions) {
    Box(Modifier.fillMaxSize()) {
        if (ui.policy.restricted) DrivingScreen(ui, actions) else StoppedScreen(ui, actions)
        if (ui.simulated) SimulationLabel(Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 24.dp))
    }
}

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
