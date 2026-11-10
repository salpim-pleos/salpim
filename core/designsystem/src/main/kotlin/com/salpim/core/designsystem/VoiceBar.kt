package com.salpim.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 모든 화면 하단의 음성 바: 마이크 | 마지막 주고받음(없으면 말할 예시) | 예시 칩 | [trailing].
 * "나"와 "살핌" 두 줄로 대화처럼 보여 준다. 말하는 동안에도 그 문장을 그대로 보여 준다.
 */
@Composable
fun VoiceBar(
    phase: VoicePhase,
    hints: List<String>,
    exchange: Exchange?,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier,
    onHintClick: ((String) -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(SalpimDimens.VoiceBarHeight)
            .background(SalpimColors.Surface)
            .padding(horizontal = SalpimDimens.ScreenPadding, vertical = SalpimDimens.ItemGap / 2)
            .testTag("voice_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SalpimDimens.SectionGap),
    ) {
        MicButton(phase, onMicClick)
        Box(Modifier.weight(CONVERSATION_WEIGHT)) {
            when {
                phase == VoicePhase.LISTENING -> Text(
                    "듣고 있어요…",
                    style = SalpimType.BodyStrong,
                    color = SalpimColors.Text,
                )
                exchange != null -> Conversation(exchange)
                onHintClick == null -> HintText(hints)
                else -> Text("이렇게 말해 보세요", style = SalpimType.Label, color = SalpimColors.TextSecondary)
            }
        }
        if (onHintClick != null) HintChips(hints, onHintClick, Modifier.weight(1f))
        trailing()
    }
}

/** 대화가 칩보다 넓다 — 대답이 잘리지 않게 */
private const val CONVERSATION_WEIGHT = 1.3f

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
        Icon(
            MdiIcons.Microphone,
            contentDescription = null,
            tint = SalpimColors.OnAction,
            modifier = Modifier.size(32.dp),
        )
    }
}

/** "나 / 살핌" 두 줄. 이름 칸 폭을 맞춰 말이 같은 세로선에서 시작한다 */
@Composable
private fun Conversation(exchange: Exchange) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        exchange.heard?.let {
            SpeakerLine("나", it, SalpimColors.TextSecondary, SalpimColors.TextSoft, maxLines = 1, tag = "heard_line")
        }
        SpeakerLine("살핌", exchange.reply, SalpimColors.Selected, SalpimColors.Text, maxLines = 2, tag = "spoken_line")
    }
}

@Composable
private fun SpeakerLine(who: String, text: String, whoColor: Color, textColor: Color, maxLines: Int, tag: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap)) {
        Text(who, style = SalpimType.LabelStrong, color = whoColor, modifier = Modifier.width(SPEAKER_WIDTH))
        Text(
            "“$text”",
            style = SalpimType.Label,
            color = textColor,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag(tag),
        )
    }
}

private val SPEAKER_WIDTH = 56.dp

@Composable
private fun HintText(hints: List<String>) {
    Column {
        val headline = hints.firstOrNull()?.let { "“$it” 하고 물어보세요" } ?: "말로 물어보세요"
        Text(headline, style = SalpimType.Label, color = SalpimColors.Text, maxLines = 1)
        hints.getOrNull(1)?.let {
            Text("“$it”도 돼요", style = SalpimType.Label, color = SalpimColors.TextSecondary, maxLines = 1)
        }
    }
}

@Composable
private fun HintChips(hints: List<String>, onHintClick: (String) -> Unit, modifier: Modifier) {
    // 좁은 분할 창에서도 칩이 잘리지 않게 가로로 밀어 볼 수 있다. 칩 묶음은 오른쪽 끝에 맞춘다
    Box(modifier, contentAlignment = Alignment.CenterEnd) {
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap),
        ) {
            HintChipList(hints, onHintClick)
        }
    }
}

@Composable
private fun HintChipList(hints: List<String>, onHintClick: (String) -> Unit) {
    hints.forEach { hint ->
        Box(
            Modifier
                .heightIn(min = SalpimDimens.TouchMin)
                .border(SalpimDimens.Line, SalpimColors.Track, RectangleShape)
                .clickable(role = Role.Button) { onHintClick(hint) }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(hint, style = SalpimType.Label, color = SalpimColors.Text, maxLines = 1)
        }
    }
}
