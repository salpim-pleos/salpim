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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.salpim.app.MainUiState
import com.salpim.app.PartStatus
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.designsystem.SalpimDimens
import com.salpim.core.designsystem.SalpimType
import com.salpim.core.designsystem.StatusIcon
import com.salpim.core.designsystem.VoiceBar

/** 칩은 두 개까지: 대화 줄이 잘리지 않고 칩도 끝까지 보이게. 다른 말은 마이크로 */
private val PARKED_HINTS = listOf("왜 그래?", "가까운 블루핸즈 찾아 줘")
private val CHOOSING_HINTS = listOf("첫 번째로 가 줘", "예약할래")
private const val TILE_COLUMNS = 3

/** 켜진 타일 뒤 면의 투명도 */
private const val ISSUE_FILL_ALPHA = 0.12f

/**
 * 정차·주차: 왼쪽 내 차 상태(부품 타일 3×2) | 오른쪽 지금 할 일. 둘 다 같은 폭, 같은 높이.
 * 타일은 남는 높이를 나눠 써서 화면 크기가 달라도 잘리지 않는다.
 */
@Composable
fun StoppedScreen(ui: MainUiState, actions: MainActions) {
    Column(Modifier.fillMaxSize().testTag("screen_stopped")) {
        Row(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SalpimDimens.SectionGap),
        ) {
            CarStatus(ui, actions, Modifier.weight(1f).fillMaxHeight())
            TodoPanel(ui, actions, Modifier.weight(1f).fillMaxHeight())
        }
        VoiceBar(
            phase = ui.voicePhase,
            hints = if (ui.inputs.places.isNullOrEmpty()) PARKED_HINTS else CHOOSING_HINTS,
            exchange = ui.exchange(),
            onMicClick = actions::onMic,
            onHintClick = actions::onHint,
            trailing = { SimulationMenu(ui, actions, enabled = true) },
        )
    }
}

@Composable
private fun CarStatus(ui: MainUiState, actions: MainActions, modifier: Modifier) {
    // 오른쪽 패널과 같은 안쪽 여백 → 두 머리줄과 아래 끝선이 맞는다
    Column(
        modifier.padding(SalpimDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap),
    ) {
        val issues = ui.parts.count { it.issue != null }
        PanelHeader(
            title = "내 차 상태",
            caption = if (issues == 0) "${ui.parts.size}곳 모두 정상" else "${ui.parts.size}곳 중 ${issues}곳 확인 필요",
        )
        Column(
            Modifier.weight(1f).testTag("part_tiles"),
            verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap),
        ) {
            ui.parts.chunked(TILE_COLUMNS).forEach { row ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap)) {
                    row.forEach { PartTile(it, actions, Modifier.weight(1f).fillMaxHeight()) }
                    repeat(TILE_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** 부품 타일. 자리는 고정, 문제 있는 타일만 긴급도 색 테두리로 켜지고 누르면 그 할 일로 */
@Composable
private fun PartTile(part: PartStatus, actions: MainActions, modifier: Modifier) {
    val issue = part.issue
    val color = issue?.urgency?.color()
    val tile = if (issue != null && color != null) {
        modifier
            .background(color.copy(alpha = ISSUE_FILL_ALPHA))
            .border(SalpimDimens.Line, color, RectangleShape)
            .clickable(role = Role.Button) { actions.selectWarning(issue.code) }
    } else {
        modifier.background(SalpimColors.Surface).border(SalpimDimens.Line, SalpimColors.Line, RectangleShape)
    }
    Column(
        tile.heightIn(min = SalpimDimens.TouchMin).padding(SalpimDimens.SmallGap / 2).testTag("part_${part.zone.name}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SalpimDimens.SmallGap / 2, Alignment.CenterVertically),
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
            maxLines = 1,
        )
    }
}
