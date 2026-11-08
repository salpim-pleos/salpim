package com.salpim.feature.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.salpim.core.model.DrivingEventType

/** 정차 후 습관 리포트 */
@Composable
fun CoachReportCard(report: CoachReport, modifier: Modifier = Modifier) {
    Column(modifier.testTag("coach_report"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("오늘의 운전 습관", style = MaterialTheme.typography.titleLarge)
        Text("${report.score}점", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DrivingEventType.entries.forEach { t ->
                Text("${t.label()} ${report.counts[t] ?: 0}회", style = MaterialTheme.typography.bodyLarge)
            }
        }
        report.tips.forEach { Text("· $it", style = MaterialTheme.typography.bodyLarge) }
    }
}
