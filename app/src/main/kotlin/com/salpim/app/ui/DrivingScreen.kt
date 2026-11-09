package com.salpim.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.salpim.app.MainUiState
import com.salpim.core.designsystem.CarTopView
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.designsystem.SalpimDimens
import com.salpim.core.designsystem.SalpimType
import com.salpim.core.designsystem.SpokenLine
import com.salpim.core.designsystem.StatusLabel
import com.salpim.core.designsystem.VoiceBar
import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.core.model.ellipsize

/** 주행 중 음성 예시 (입력 없음, 말로 묻는다) */
private val DRIVING_HINTS = listOf("차 상태 어때?", "얼마나 더 갈 수 있어?")

/**
 * 주행 중: 정보 3개 이하 (상태 / 연료 / 점수), 스크롤·입력 없음, 하단 음성 바 (AGENTS.md 3절).
 */
@Composable
fun DrivingScreen(ui: MainUiState, actions: MainActions) {
    Column(Modifier.fillMaxSize().testTag("screen_driving")) {
        Row(
            Modifier.weight(1f).fillMaxWidth().padding(SalpimDimens.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(SalpimDimens.SectionGap),
            verticalAlignment = Alignment.Top,
        ) {
            val top = ui.topWarning
            CarTopView(
                litZone = top?.zone,
                litColor = top?.urgency?.color() ?: SalpimColors.Caution,
                description = top?.let { "내 차, ${it.title}" } ?: "내 차, 모든 곳 정상",
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap)) {
                StatusBlock(top, ui.policy.maxStringLength)
                Spacer(Modifier.height(8.dp))
                Divider()
                FuelAndScore(ui)
                ui.inputs.lastReply?.let { SpokenLine(it) }
            }
        }
        VoiceBar(phase = ui.voicePhase, hints = DRIVING_HINTS, onMicClick = actions::onMic)
    }
}

@Composable
private fun StatusBlock(top: Explanation?, maxChars: Int?) {
    if (top == null) {
        Text("모두 괜찮아요", style = SalpimType.HeadlineSmall, color = SalpimColors.Text)
        Text("엔진, 브레이크, 안전벨트까지 살펴봤어요", style = SalpimType.Label, color = SalpimColors.TextSoft)
        return
    }
    StatusLabel(top.urgency.label(), top.urgency.color(), urgent = top.urgency == Urgency.IMMEDIATE_STOP)
    Text(
        top.oneLine.substringBefore(SENTENCE_END).ellipsize(maxChars),
        style = SalpimType.HeadlineSmall,
        color = SalpimColors.Text,
        modifier = Modifier.testTag("warning_banner"),
    )
    val plain = listOfNotNull(top.plainMeaning?.let { "$it 이에요." }, top.oneLine.substringAfter(SENTENCE_END, "").trim())
    Text(plain.joinToString(" ").ellipsize(maxChars?.times(2)), style = SalpimType.Label, color = SalpimColors.TextSoft)
}

@Composable
private fun ColumnScope.FuelAndScore(ui: MainUiState) {
    ui.fuel?.let {
        Text(
            it.text,
            style = SalpimType.BodyStrong,
            color = if (it.needsRefuel) SalpimColors.Caution else SalpimColors.Text,
            modifier = Modifier.testTag("fuel_line"),
        )
    }
    Text(
        "오늘 안전 점수 ${ui.report.score}점",
        style = SalpimType.Label,
        color = SalpimColors.TextSecondary,
        modifier = Modifier.testTag("score_line"),
    )
}

@Composable
internal fun Divider() {
    Spacer(Modifier.fillMaxWidth().height(SalpimDimens.Line).background(SalpimColors.Line))
}

private const val SENTENCE_END = ". "
