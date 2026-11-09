package com.salpim.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.salpim.app.MainUiState
import com.salpim.app.PartStatus
import com.salpim.core.designsystem.CarTopView
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.designsystem.SalpimDimens
import com.salpim.core.designsystem.SalpimType
import com.salpim.core.designsystem.SecondaryButton
import com.salpim.core.designsystem.SpokenLine
import com.salpim.core.designsystem.VoiceBar
import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency

private val PARKED_HINTS = listOf("왜 그래?", "가까운 블루핸즈 찾아 줘", "얼마나 더 갈 수 있어?")
private val CHOOSING_HINTS = listOf("첫 번째로 가 줘", "다른 데", "예약할래")
private const val CAR_WIDTH_DP = 180

/** 정차·주차: 내 차 상태(그림 + 부품) / 지금 할 일 / 음성 바 */
@Composable
fun StoppedScreen(ui: MainUiState, actions: MainActions) {
    Column(Modifier.fillMaxSize().testTag("screen_stopped")) {
        Row(Modifier.weight(1f).fillMaxWidth()) {
            CarStatusColumn(ui, actions, Modifier.weight(1.1f).fillMaxHeight())
            TodoPanel(ui, actions, Modifier.weight(1f).fillMaxHeight())
        }
        ui.inputs.lastReply?.let { SpokenLine(it) }
        VoiceBar(
            phase = ui.voicePhase,
            hints = if (ui.inputs.places.isNullOrEmpty()) PARKED_HINTS else CHOOSING_HINTS,
            onMicClick = actions::onMic,
            onHintClick = actions::onHint,
        )
    }
}

@Composable
private fun CarStatusColumn(ui: MainUiState, actions: MainActions, modifier: Modifier) {
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(SalpimDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap),
    ) {
        Text("내 차 상태", style = SalpimType.TitleSmall, color = SalpimColors.Text)
        val issues = ui.parts.count { it.issue != null }
        val summary = if (issues == 0) "${ui.parts.size}곳을 살펴봤어요. 모두 괜찮아요." else "${ui.parts.size}곳을 살펴봤어요. ${issues}곳만 점검하면 돼요."
        Text(summary, style = SalpimType.Label, color = SalpimColors.TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(SalpimDimens.SectionGap)) {
            val top = ui.topWarning
            CarTopView(
                litZone = top?.zone,
                litColor = top?.urgency?.color() ?: SalpimColors.Caution,
                description = "내 차 그림",
                width = CAR_WIDTH_DP.dp,
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { ui.parts.forEach { PartRow(it) } }
        }
        WarningHistory(ui.warningList, actions)
        ScenarioButtons(ui, actions)
    }
}

@Composable
private fun PartRow(part: PartStatus) {
    val issue = part.issue
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val color = issue?.urgency?.color() ?: SalpimColors.TextTertiary
        Box(
            Modifier.size(
                14.dp,
            ).background(color, if (issue?.urgency == Urgency.IMMEDIATE_STOP) RectangleShape else CircleShape),
        )
        Column {
            Text(
                if (issue == null) "${part.name} 정상" else part.name,
                style = SalpimType.LabelStrong,
                color = issue?.urgency?.color() ?: SalpimColors.Text,
            )
            Text(part.plainMeaning, style = SalpimType.Caption, color = SalpimColors.TextSecondary)
        }
    }
}

@Composable
private fun WarningHistory(warnings: List<Explanation>, actions: MainActions) {
    if (warnings.isEmpty()) {
        Text(
            "오늘 받은 경고가 없어요",
            style = SalpimType.Label,
            color = SalpimColors.TextSecondary,
            modifier = Modifier.testTag("no_warnings"),
        )
        return
    }
    Text("오늘 받은 경고", style = SalpimType.LabelStrong, color = SalpimColors.TextSecondary)
    warnings.forEach { e ->
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = SalpimDimens.TouchMin)
                .background(SalpimColors.Surface)
                .clickable(role = Role.Button) { actions.selectWarning(e.code) }
                .padding(horizontal = 20.dp)
                .testTag("warning_item_${e.code.value}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(e.title, style = SalpimType.Label, color = SalpimColors.Text)
            Text(e.urgency.label(), style = SalpimType.Label, color = e.urgency.color())
        }
    }
}

@Composable
private fun ScenarioButtons(ui: MainUiState, actions: MainActions) {
    if (ui.scenarios.isEmpty()) return
    Text("시뮬레이션 시나리오", style = SalpimType.LabelStrong, color = SalpimColors.Simulated)
    ui.scenarios.forEach { s ->
        val title = if (ui.playingScenario == s.id) "재생 중: ${s.title}" else s.title
        SecondaryButton(
            title,
            onClick = { actions.playScenario(s.id) },
            modifier = Modifier.testTag("scenario_${s.id}"),
        )
    }
}
