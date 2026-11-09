package com.salpim.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** 주 액션: 현대 네이비 직사각형 (docs/DESIGN.md 원칙 5) */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .heightIn(min = SalpimDimens.PrimaryButtonHeight)
            .background(SalpimColors.Action, RectangleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text, style = SalpimType.BodyStrong, color = SalpimColors.OnAction)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .heightIn(min = SalpimDimens.TouchMin)
            .border(SalpimDimens.Line, SalpimColors.TextTertiary, RectangleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text, style = SalpimType.LabelStrong, color = SalpimColors.Text)
    }
}

/** 시뮬레이션 신호일 때 항상 보이는 표시 (AGENTS.md 2절) */
@Composable
fun SimulationLabel(modifier: Modifier = Modifier) {
    Text(
        "시뮬레이션 신호",
        modifier = modifier.testTag("badge_simulated"),
        style = SalpimType.Caption,
        color = SalpimColors.Simulated,
    )
}

/** 앱이 한 말. 소리와 함께 화면에도 남긴다 */
@Composable
fun SpokenLine(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().background(SalpimColors.Surface).padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            SalpimIcons.Speaker,
            contentDescription = null,
            tint = SalpimColors.TextSoft,
            modifier = Modifier.size(28.dp),
        )
        Text(
            "“$text”",
            style = SalpimType.BodySmall,
            color = SalpimColors.Text,
            modifier = Modifier.testTag("spoken_line"),
        )
    }
}

/** 상태 표시: 색만 쓰지 않고 모양 + 글자를 함께 */
@Composable
fun StatusLabel(text: String, color: Color, urgent: Boolean, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(20.dp).background(color, if (urgent) RectangleShape else CircleShape))
        Text(text, style = SalpimType.LabelStrong, color = color)
    }
}

enum class VoicePhase { IDLE, LISTENING, SPEAKING }

/** 모든 화면 하단의 음성 바: 마이크 + 말할 예시 */
@Composable
fun VoiceBar(
    phase: VoicePhase,
    hints: List<String>,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier,
    onHintClick: ((String) -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth().background(
            SalpimColors.Surface,
        ).padding(horizontal = 32.dp, vertical = 16.dp).testTag("voice_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        MicButton(phase, onMicClick)
        if (onHintClick == null) {
            VoiceHintText(phase, hints)
        } else {
            VoiceHintChips(hints, onHintClick)
        }
    }
}

@Composable
private fun MicButton(phase: VoicePhase, onClick: () -> Unit) {
    val listening = phase == VoicePhase.LISTENING
    Box(
        Modifier
            .size(SalpimDimens.TouchMin)
            .background(if (listening) SalpimColors.Notice else SalpimColors.Action, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = if (listening) "듣는 중" else "말하기" }
            .testTag("btn_mic"),
        contentAlignment = Alignment.Center,
    ) {
        Icon(SalpimIcons.Mic, contentDescription = null, tint = SalpimColors.OnAction, modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun VoiceHintText(phase: VoicePhase, hints: List<String>) {
    Column {
        val headline = when (phase) {
            VoicePhase.LISTENING -> "듣고 있어요"
            VoicePhase.SPEAKING -> "말하는 중이에요"
            VoicePhase.IDLE -> hints.firstOrNull()?.let { "“$it” 하고 물어보세요" } ?: "말로 물어보세요"
        }
        Text(headline, style = SalpimType.Label, color = SalpimColors.Text)
        hints.drop(
            1,
        ).firstOrNull()?.let { Text("“$it”도 돼요", style = SalpimType.Label, color = SalpimColors.TextSecondary) }
    }
}

@Composable
private fun VoiceHintChips(hints: List<String>, onHintClick: (String) -> Unit) {
    // 좁은 분할 창에서도 칩이 잘리지 않게 가로로 밀어 볼 수 있다
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "이렇게 말해 보세요",
            style = SalpimType.Label,
            color = SalpimColors.TextSecondary,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
        hints.forEach { hint ->
            Box(
                Modifier
                    .heightIn(min = SalpimDimens.TouchMin)
                    .border(SalpimDimens.Line, SalpimColors.Track, RectangleShape)
                    .clickable(role = Role.Button) { onHintClick(hint) }
                    .padding(horizontal = 22.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(hint, style = SalpimType.Label, color = SalpimColors.Text)
            }
        }
    }
}
