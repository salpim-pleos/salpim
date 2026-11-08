package com.salpim.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salpim.app.MainUiState
import com.salpim.core.model.DrivingState
import com.salpim.core.model.WarningCode
import com.salpim.feature.coach.CoachReportCard
import com.salpim.feature.glance.GlanceRow
import com.salpim.feature.service.ServiceNavigateButton
import com.salpim.feature.warning.WarningBanner
import com.salpim.feature.warning.WarningDetail
import com.salpim.feature.warning.color
import com.salpim.feature.warning.label

@Composable
fun SalpimTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, content = content)
    }
}

/** UxPolicy가 제한이면 주행 화면, 아니면 정차 화면. 시뮬레이션이면 항상 배지. */
@Composable
fun SalpimRoot(
    ui: MainUiState,
    onSelectWarning: (WarningCode) -> Unit,
    onNavigateService: () -> Unit,
    onPlayScenario: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        if (ui.policy.restricted) {
            DrivingScreen(ui)
        } else {
            StoppedScreen(ui, onSelectWarning, onNavigateService, onPlayScenario, onClearHistory)
        }
        if (ui.simulated) SimulatedBadge(Modifier.align(Alignment.TopEnd).padding(12.dp))
    }
}

@Composable
fun SimulatedBadge(modifier: Modifier = Modifier) {
    Text(
        "SIMULATED",
        modifier = modifier
            .background(Color(0xFF7E57C2), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("badge_simulated"),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
    )
}

private fun DrivingState.label() = when (this) {
    DrivingState.DRIVING -> "주행 중"
    DrivingState.STOPPED -> "정차"
    DrivingState.PARKED -> "주차"
}

/** 입력 없음, 스크롤 없음, 정보 최대 3개 (배너 + 글랜스) */
@Composable
private fun DrivingScreen(ui: MainUiState) {
    Column(
        Modifier.fillMaxSize().padding(32.dp).testTag("screen_driving"),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(ui.drivingState.label(), fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ui.banner?.let { WarningBanner(it, ui.policy.maxStringLength) }
        GlanceRow(ui.glance, ui.policy.maxStringLength)
    }
}

@Composable
private fun StoppedScreen(
    ui: MainUiState,
    onSelectWarning: (WarningCode) -> Unit,
    onNavigateService: () -> Unit,
    onPlayScenario: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    Row(
        Modifier.fillMaxSize().padding(24.dp).testTag("screen_stopped"),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            Modifier.weight(1.4f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(ui.drivingState.label(), style = MaterialTheme.typography.titleMedium)
            Text("경고 안내", style = MaterialTheme.typography.titleLarge)
            if (ui.warningList.isEmpty()) {
                Text("받은 경고가 없어요", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("no_warnings"))
            }
            ui.warningList.forEach { e ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .clickable { onSelectWarning(e.code) }
                        .padding(16.dp)
                        .testTag("warning_item_${e.code.value}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(e.title, style = MaterialTheme.typography.titleMedium)
                    Text(e.urgency.label(), color = e.urgency.color())
                }
            }
            ui.selected?.let { e ->
                HorizontalDivider()
                WarningDetail(e) {
                    if (e.needsService) ServiceNavigateButton(onNavigateService)
                    ui.navMessage?.let { Text(it, modifier = Modifier.testTag("nav_message")) }
                }
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CoachReportCard(ui.report)
            OutlinedButton(onClick = onClearHistory, modifier = Modifier.testTag("btn_clear_history")) {
                Text("기록 삭제")
            }
            if (ui.scenarios.isNotEmpty()) {
                HorizontalDivider()
                Text("시뮬레이션 시나리오", style = MaterialTheme.typography.titleMedium)
                ui.scenarios.forEach { s ->
                    OutlinedButton(onClick = { onPlayScenario(s.id) }, modifier = Modifier.testTag("scenario_${s.id}")) {
                        Text(if (ui.playingScenario == s.id) "재생 중: ${s.title}" else s.title)
                    }
                }
            }
        }
    }
}
