package com.salpim.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextOverflow
import com.salpim.app.FuelSummary
import com.salpim.app.MainUiState
import com.salpim.app.RouteProgress
import com.salpim.core.designsystem.MdiIcons
import com.salpim.core.designsystem.RangeBar
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.designsystem.SalpimDimens
import com.salpim.core.designsystem.SalpimType
import com.salpim.core.designsystem.StatusIcon
import com.salpim.core.designsystem.VoiceBar
import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.core.model.ellipsize

/** 주행 중 음성 예시 (입력 없음, 말로 묻는다) */
private val DRIVING_HINTS = listOf("차 상태 어때?", "얼마나 더 갈 수 있어?")

/** 상태 카드가 조금 더 넓다 — 가장 중요한 정보 */
private const val STATUS_WEIGHT = 1.15f

/**
 * 주행 중: 정보 3개(상태 / 남은 거리 / 점수)를 카드 3장으로, 스크롤·입력 없음, 하단 음성 바 (AGENTS.md 3절).
 * 왼쪽 큰 상태 카드 | 오른쪽 연료·점수 카드를 위아래로. 높이를 나눠 써서 빈 곳 없이 꽉 차게.
 */
@Composable
fun DrivingScreen(ui: MainUiState, actions: MainActions) {
    Column(Modifier.fillMaxSize().testTag("screen_driving")) {
        Row(Modifier.weight(1f).fillMaxWidth()) {
            // 시뮬레이션에서는 실제 차의 화면 분할처럼 왼쪽에 내비 자리 그림을 둔다
            if (ui.simulated) SimNaviPane(ui, Modifier.weight(NAVI_WEIGHT).fillMaxHeight())
            Dashboard(ui, Modifier.weight(1f).fillMaxHeight())
        }
        VoiceBar(
            phase = ui.voicePhase,
            hints = DRIVING_HINTS,
            exchange = ui.exchange(),
            onMicClick = actions::onMic,
            trailing = { SimulationMenu(ui, actions, enabled = false) },
        )
    }
}

/** 왼쪽 큰 상태 카드 | 오른쪽 연료·점수 카드를 위아래로. 높이를 나눠 써서 빈 곳 없이 꽉 차게 */
@Composable
private fun Dashboard(ui: MainUiState, modifier: Modifier) {
    Row(
        modifier.padding(SalpimDimens.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(SalpimDimens.SectionGap),
    ) {
        StatusCard(
            ui.topWarning,
            ui.parts.size,
            ui.policy.maxStringLength,
            Modifier.weight(STATUS_WEIGHT).fillMaxHeight(),
        )
        Column(
            Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(SalpimDimens.SectionGap),
        ) {
            FuelCard(ui.fuel, ui.route, Modifier.weight(1f).fillMaxWidth())
            ScoreCard(ui.report.score, Modifier.weight(1f).fillMaxWidth())
        }
    }
}

/** 시연용 내비 그림 폭 (살핌 대시보드 1 대비) */
private const val NAVI_WEIGHT = 0.8f

/** 문제 부위 아이콘이 한 번 밝아지며 켜지고, 아래에 쉬운 말 한 줄 + 긴급도. 카드 가운데 정렬 */
@Composable
private fun StatusCard(top: Explanation?, partCount: Int, maxChars: Int?, modifier: Modifier) {
    Column(
        modifier.background(SalpimColors.Surface).padding(SalpimDimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap, Alignment.CenterVertically),
    ) {
        if (top == null) {
            StatusIcon(MdiIcons.CheckCircle, description = null, litColor = null)
            Text("모두 괜찮아요", style = SalpimType.HeadlineSmall, color = SalpimColors.Text)
            Text("${partCount}곳 정상", style = SalpimType.Label, color = SalpimColors.TextSoft)
            return@Column
        }
        StatusIcon(top.icon(), description = top.title, litColor = top.urgency.color())
        Text(
            top.oneLine.substringBefore(SENTENCE_END).ellipsize(maxChars),
            style = SalpimType.TitleSmall,
            color = SalpimColors.Text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("warning_banner"),
        )
        UrgencyLabel(top.urgency)
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

/**
 * 주유 아이콘 + 남은 거리 숫자, 아래에 막대와 목적지. 목적지는 주유 판단(필요 거리)의 근거라 같은 카드에 둔다.
 * 문장은 접근성 설명으로 읽힌다.
 */
@Composable
private fun FuelCard(fuel: FuelSummary?, route: RouteProgress?, modifier: Modifier) {
    val color = if (fuel?.needsRefuel == true) SalpimColors.Caution else SalpimColors.Text
    val spoken = listOfNotNull(fuel?.text ?: "연료 정보를 기다리는 중", route?.text).joinToString(", ")
    Column(
        modifier
            .background(SalpimColors.Surface)
            .padding(SalpimDimens.ScreenPadding)
            .testTag("fuel_line")
            .clearAndSetSemantics { contentDescription = spoken },
        verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap, Alignment.CenterVertically),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap),
        ) {
            Icon(
                MdiIcons.GasStation,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(SalpimDimens.LineIcon),
            )
            Text(fuel?.rangeText ?: "—", style = SalpimType.TitleSmall, color = color)
        }
        fuel?.let {
            val barColor = if (it.needsRefuel) SalpimColors.Caution else SalpimColors.TextSoft
            RangeBar(it.bar.conservativeFraction, it.bar.likelyFraction, barColor, needFraction = it.bar.needFraction)
        }
        route?.let { Text(it.text, style = SalpimType.Label, color = SalpimColors.TextSoft, maxLines = 1) }
    }
}

@Composable
private fun ScoreCard(score: Int, modifier: Modifier) {
    Row(
        modifier.background(SalpimColors.Surface).padding(SalpimDimens.ScreenPadding).testTag("score_line"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap),
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

private const val SENTENCE_END = ". "
