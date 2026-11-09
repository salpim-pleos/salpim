package com.salpim.feature.warning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.core.model.ellipsize

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

/** 주행 중 한 줄 안내. 터치 불필요, 한 줄 고정. */
@Composable
fun WarningBanner(explanation: Explanation, maxChars: Int?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(explanation.urgency.color().copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("warning_banner"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UrgencyDot(explanation.urgency)
        Text(
            text = explanation.oneLine.ellipsize(maxChars),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 정차/주차 시 상세. 스크롤 컨테이너 안에서 쓴다. */
@Composable
fun WarningDetail(explanation: Explanation, modifier: Modifier = Modifier, footer: @Composable () -> Unit = {}) {
    Column(modifier.testTag("warning_detail"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UrgencyDot(explanation.urgency)
            Text(explanation.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                explanation.urgency.label(),
                color = explanation.urgency.color(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Text(explanation.detail, style = MaterialTheme.typography.bodyLarge)
        if (explanation.causes.isNotEmpty()) {
            Text("가능한 원인", style = MaterialTheme.typography.titleMedium)
            explanation.causes.forEach { Text("· $it", style = MaterialTheme.typography.bodyLarge) }
        }
        Text("해야 할 일", style = MaterialTheme.typography.titleMedium)
        explanation.actions.forEachIndexed { i, a -> Text("${i + 1}. $a", style = MaterialTheme.typography.bodyLarge) }
        Text(
            "이 안내는 참고용이에요. 정확한 판단은 정비사에게 확인하세요.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        footer()
    }
}

@Composable
private fun UrgencyDot(urgency: Urgency) {
    Row(Modifier.size(20.dp).background(urgency.color(), CircleShape)) {}
}
