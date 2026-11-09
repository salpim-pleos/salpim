package com.salpim.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.salpim.app.FuelSummary
import com.salpim.app.MainUiState
import com.salpim.core.designsystem.MdiIcons
import com.salpim.core.designsystem.RangeBar
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.designsystem.SalpimDimens
import com.salpim.core.designsystem.SalpimType
import com.salpim.core.designsystem.SpokenLine
import com.salpim.core.designsystem.StatusIcon
import com.salpim.core.designsystem.VoiceBar
import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.core.model.ellipsize

/** 주행 중 음성 예시 (입력 없음, 말로 묻는다) */
private val DRIVING_HINTS = listOf("차 상태 어때?", "얼마나 더 갈 수 있어?")

/**
 * 주행 중: 정보 3개 이하 (상태 / 연료 / 점수), 스크롤·입력 없음, 하단 음성 바 (AGENTS.md 3절).
 * 글보다 아이콘·색·막대로 읽히게 한다. 글은 상태 한 줄과 숫자뿐.
 */
@Composable
fun DrivingScreen(ui: MainUiState, actions: MainActions) {
    Column(Modifier.fillMaxSize().testTag("screen_driving")) {
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(SalpimDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(SalpimDimens.SectionGap),
        ) {
            StatusCard(ui.topWarning, ui.parts.size, ui.policy.maxStringLength)
            ui.fuel?.let { FuelRow(it) }
            ScoreRow(ui.report.score)
            ui.inputs.lastReply?.let { SpokenLine(it) }
        }
        VoiceBar(phase = ui.voicePhase, hints = DRIVING_HINTS, onMicClick = actions::onMic)
    }
}

/** 문제 부위 아이콘이 한 번 밝아지며 켜지고, 옆에 쉬운 말 한 줄 + 긴급도 */
@Composable
private fun StatusCard(top: Explanation?, partCount: Int, maxChars: Int?) {
    Row(
        Modifier.fillMaxWidth().background(SalpimColors.Surface).padding(SalpimDimens.SectionGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SalpimDimens.SectionGap),
    ) {
        if (top == null) {
            StatusIcon(MdiIcons.CheckCircle, description = null, litColor = null)
            Column {
                Text("모두 괜찮아요", style = SalpimType.HeadlineSmall, color = SalpimColors.Text)
                Text("${partCount}곳 정상", style = SalpimType.Label, color = SalpimColors.TextSoft)
            }
            return@Row
        }
        StatusIcon(top.icon(), description = top.title, litColor = top.urgency.color())
        Column(verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap)) {
            Text(
                top.oneLine.substringBefore(SENTENCE_END).ellipsize(maxChars),
                style = SalpimType.TitleSmall,
                color = SalpimColors.Text,
                modifier = Modifier.testTag("warning_banner"),
            )
            UrgencyLabel(top.urgency)
        }
    }
}

/** 색 + 모양(즉시 정차는 네모)으로 긴급도. 색만으로 구분하지 않는다 */
@Composable
internal fun UrgencyLabel(urgency: Urgency) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap),
    ) {
        val shape = if (urgency == Urgency.IMMEDIATE_STOP) RectangleShape else CircleShape
        Box(Modifier.size(SalpimDimens.StatusDot).background(urgency.color(), shape))
        Text(urgency.label(), style = SalpimType.LabelStrong, color = urgency.color())
    }
}

/** 주유 아이콘 + 남은 거리 막대 + 숫자. 문장은 접근성 설명으로 읽힌다 */
@Composable
private fun FuelRow(fuel: FuelSummary) {
    val color = if (fuel.needsRefuel) SalpimColors.Caution else SalpimColors.Text
    Row(
        Modifier.fillMaxWidth().testTag("fuel_line").clearAndSetSemantics { contentDescription = fuel.text },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ButtonIconGap),
    ) {
        Icon(
            MdiIcons.GasStation,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(SalpimDimens.LineIcon),
        )
        val barColor = if (fuel.needsRefuel) SalpimColors.Caution else SalpimColors.TextSoft
        RangeBar(
            fuel.bar.conservativeFraction,
            fuel.bar.likelyFraction,
            barColor,
            Modifier.weight(1f),
            fuel.bar.needFraction,
        )
        Text(fuel.rangeText, style = SalpimType.TitleSmall, color = color)
    }
}

@Composable
private fun ScoreRow(score: Int) {
    Row(
        Modifier.testTag("score_line"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ButtonIconGap),
    ) {
        Icon(
            MdiIcons.Gauge,
            contentDescription = null,
            tint = SalpimColors.Text,
            modifier = Modifier.size(SalpimDimens.LineIcon),
        )
        Text("$score", style = SalpimType.HeadlineSmall, color = SalpimColors.Text)
        Text("안전 점수", style = SalpimType.Label, color = SalpimColors.TextSecondary)
    }
}

@Composable
internal fun Divider() {
    Box(Modifier.fillMaxWidth().height(SalpimDimens.Line).background(SalpimColors.Line))
}

private const val SENTENCE_END = ". "
