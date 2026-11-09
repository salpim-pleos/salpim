package com.salpim.feature.glance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.core.model.WarningCodes
import com.salpim.core.model.ellipsize

enum class GlanceTone { OK, CAUTION, ALERT }

data class GlanceItem(val id: String, val label: String, val value: String, val tone: GlanceTone)

/**
 * 주행 중 표시 항목. 최대 [maxItems]개 (안전/UX 규칙 1: 최대 3개).
 * 속도/RPM 같은 시스템 정보는 중복하지 않고 "지금 신경 써야 할 것"만.
 */
object GlanceModel {
    fun build(warnings: List<Explanation>, safetyScore: Int, maxItems: Int = 3): List<GlanceItem> {
        val vehicle = warnings.filterNot { it.code in WarningCodes.ENERGY }
        val energy = warnings.filter { it.code in WarningCodes.ENERGY }
        val top = vehicle.minByOrNull { it.urgency.ordinal }

        val items = listOf(
            GlanceItem(
                id = "warning",
                label = "차량 경고",
                value = top?.title ?: "이상 없음",
                tone = when (top?.urgency) {
                    null -> GlanceTone.OK
                    Urgency.IMMEDIATE_STOP -> GlanceTone.ALERT
                    else -> GlanceTone.CAUTION
                },
            ),
            GlanceItem(
                id = "energy",
                label = "연료·배터리",
                value = energy.firstOrNull()?.title ?: "충분",
                tone = if (energy.isEmpty()) GlanceTone.OK else GlanceTone.CAUTION,
            ),
            GlanceItem(
                id = "score",
                label = "오늘의 안전 점수",
                value = "${safetyScore}점",
                tone = if (safetyScore >= GOOD_SCORE) GlanceTone.OK else GlanceTone.CAUTION,
            ),
        )
        return items.take(maxItems.coerceAtMost(MAX_ITEMS))
    }

    private const val MAX_ITEMS = 3
    private const val GOOD_SCORE = 80
}

private fun GlanceTone.color() = when (this) {
    GlanceTone.OK -> SalpimColors.Ok
    GlanceTone.CAUTION -> SalpimColors.Caution
    GlanceTone.ALERT -> SalpimColors.Urgent
}

/** 큰 글씨, 입력 없음, 스크롤 없음 */
@Composable
fun GlanceRow(items: List<GlanceItem>, maxChars: Int?, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().testTag("glance"), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items.forEach { item ->
            Column(
                Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(20.dp)
                    .testTag("glance_${item.id}"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(item.label, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text(
                    item.value.ellipsize(maxChars),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = item.tone.color(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
