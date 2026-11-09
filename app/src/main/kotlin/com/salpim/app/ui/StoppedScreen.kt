package com.salpim.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.salpim.app.MainUiState
import com.salpim.app.PartStatus
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.designsystem.SalpimDimens
import com.salpim.core.designsystem.SalpimType
import com.salpim.core.designsystem.SecondaryButton
import com.salpim.core.designsystem.SpokenLine
import com.salpim.core.designsystem.StatusIcon
import com.salpim.core.designsystem.VoiceBar
import com.salpim.core.model.Explanation

private val PARKED_HINTS = listOf("왜 그래?", "가까운 블루핸즈 찾아 줘", "얼마나 더 갈 수 있어?")
private val CHOOSING_HINTS = listOf("첫 번째로 가 줘", "다른 데", "예약할래")
private const val TILE_COLUMNS = 3

/** 켜진 타일 뒤 면의 투명도 */
private const val ISSUE_FILL_ALPHA = 0.12f

/** 정차·주차: 내 차 상태(부품 타일) / 지금 할 일 / 음성 바 */
@Composable
fun StoppedScreen(ui: MainUiState, actions: MainActions) {
    Column(Modifier.fillMaxSize().testTag("screen_stopped")) {
        Row(Modifier.weight(1f).fillMaxWidth()) {
            CarStatusColumn(ui, actions, Modifier.weight(1.25f).fillMaxHeight())
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
        PartTiles(ui.parts, actions)
        WarningHistory(ui.warningList, actions)
        ScenarioButtons(ui, actions)
    }
}

/** 부품 6곳을 3×2 고정 자리에. 문제 부위만 긴급도 색으로 켜진다 */
@Composable
private fun PartTiles(parts: List<PartStatus>, actions: MainActions) {
    Column(Modifier.testTag("part_tiles"), verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap)) {
        parts.chunked(TILE_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap)) {
                row.forEach { PartTile(it, actions, Modifier.weight(1f)) }
                repeat(TILE_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun PartTile(part: PartStatus, actions: MainActions, modifier: Modifier) {
    val issue = part.issue
    val color = issue?.urgency?.color()
    val tileModifier = if (issue != null && color != null) {
        modifier
            .background(color.copy(alpha = ISSUE_FILL_ALPHA))
            .border(SalpimDimens.Line, color, RectangleShape)
            .clickable(role = Role.Button) { actions.selectWarning(issue.code) }
    } else {
        modifier.background(SalpimColors.Surface).border(SalpimDimens.Line, SalpimColors.Line, RectangleShape)
    }
    Column(
        tileModifier.heightIn(
            min = SalpimDimens.TileHeight,
        ).padding(SalpimDimens.ItemGap).testTag("part_${part.zone.name}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        StatusIcon(
            issue?.icon() ?: part.zone.icon(),
            description = null,
            litColor = color,
            boxSize = SalpimDimens.TileIcon,
            iconSize = SalpimDimens.TileIcon,
        )
        Text(
            part.name,
            style = SalpimType.LabelStrong,
            color = SalpimColors.Text,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            issue?.urgency?.label() ?: "정상",
            style = SalpimType.Caption,
            color = color ?: SalpimColors.TextSecondary,
        )
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
            UrgencyLabel(e.urgency)
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
