package com.salpim.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.salpim.app.MainUiState
import com.salpim.app.Todo
import com.salpim.core.designsystem.MdiIcons
import com.salpim.core.designsystem.PrimaryButton
import com.salpim.core.designsystem.QrCode
import com.salpim.core.designsystem.RangeBar
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.designsystem.SalpimDimens
import com.salpim.core.designsystem.SalpimType
import com.salpim.core.designsystem.SecondaryButton
import com.salpim.feature.service.PlaceCategory
import com.salpim.feature.service.ReservationLink

private const val SENTENCE_END = ". "
private const val DETAIL_LINES = 2

/** 경고 할 일: 제목 · 쉬운 설명 · 첫 조치, 맨 아래에 블루핸즈 안내와 휴대폰 예약 */
@Composable
internal fun ColumnScope.ServiceContent(todo: Todo.Service, actions: MainActions) {
    val e = todo.explanation
    Column(
        Modifier.weight(1f).testTag("warning_detail"),
        verticalArrangement = Arrangement.spacedBy(SalpimDimens.SmallGap),
    ) {
        // 긴급도는 왼쪽 타일과 위 칩에 이미 색으로 보이므로 여기서는 아이콘 색으로만
        PanelIconTitle(e.icon(), e.urgency.color(), e.oneLine.substringBefore(SENTENCE_END))
        Text(
            e.detail,
            style = SalpimType.Label,
            color = SalpimColors.TextSoft,
            maxLines = DETAIL_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))
        Text("정확한 상태는 정비사가 확인해요. 이 안내는 참고용이에요.", style = SalpimType.Caption, color = SalpimColors.TextSecondary)
    }
    if (e.needsService) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap)) {
            PrimaryButton(
                "블루핸즈로 안내",
                onClick = { actions.findNearby(PlaceCategory.BLUE_HANDS) },
                icon = MdiIcons.CarWrench,
                centered = true,
                modifier = Modifier.weight(1f).testTag("btn_navigate_service"),
            )
            SecondaryButton(
                "휴대폰 예약",
                onClick = actions::showReservation,
                icon = MdiIcons.QrCode,
                centered = true,
                modifier = Modifier.weight(
                    1f,
                ).heightIn(min = SalpimDimens.PrimaryButtonHeight).testTag("btn_reservation"),
            )
        }
    }
}

/** 주유 할 일: 판단 한 줄 · 남은 거리 막대 · 목적지, 맨 아래에 가까운 주유소 */
@Composable
internal fun ColumnScope.RefuelContent(todo: Todo.Refuel, ui: MainUiState, actions: MainActions) {
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap)) {
        PanelIconTitle(MdiIcons.GasStation, SalpimColors.Caution, todo.fuel.text)
        RangeBar(
            todo.fuel.bar.conservativeFraction,
            todo.fuel.bar.likelyFraction,
            SalpimColors.Caution,
            needFraction = todo.fuel.bar.needFraction,
        )
        Text(
            listOfNotNull(ui.route?.text, "진한 막대 = 거의 확실, 옅은 막대 = 아마도").joinToString(" · "),
            style = SalpimType.Caption,
            color = SalpimColors.TextSecondary,
            maxLines = 1,
        )
    }
    PrimaryButton(
        "가까운 주유소",
        onClick = { actions.findNearby(PlaceCategory.GAS_STATION) },
        icon = MdiIcons.GasStation,
        centered = true,
        modifier = Modifier.fillMaxWidth().testTag("btn_find_gas"),
    )
}

/** 휴대폰 예약: 왼쪽 QR, 오른쪽 설명. 주소도 글자로 남긴다 (QR을 못 찍는 경우) */
@Composable
internal fun ColumnScope.ReservationPanel(actions: MainActions) {
    PanelHeader("휴대폰으로 예약", onClose = actions::closePanel)
    Row(
        Modifier.weight(1f).fillMaxWidth().testTag("reservation_panel"),
        horizontalArrangement = Arrangement.spacedBy(SalpimDimens.SectionGap),
    ) {
        QrCode(ReservationLink.OFFICIAL_RESERVATION_URL, description = "현대 공식 정비 예약 페이지 QR")
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap)) {
            Text("휴대폰 카메라로 찍으면 현대 공식 정비 예약 페이지가 열려요.", style = SalpimType.Label, color = SalpimColors.Text)
            Text("예약은 3일 뒤부터 할 수 있어요.", style = SalpimType.Label, color = SalpimColors.TextSoft)
            Text(
                ReservationLink.OFFICIAL_RESERVATION_URL,
                style = SalpimType.Caption,
                color = SalpimColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 할 일이 없을 때: 오늘 운전 점수와 팁, 맨 아래에 기록 지우기 */
@Composable
internal fun ColumnScope.NothingPanel(ui: MainUiState, actions: MainActions) {
    PanelHeader("지금 할 일")
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap)) {
        PanelIconTitle(MdiIcons.CheckCircle, SalpimColors.Text, "지금은 할 일이 없어요")
        Row(
            Modifier.testTag("coach_report"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap),
        ) {
            Icon(
                MdiIcons.Gauge,
                contentDescription = null,
                tint = SalpimColors.TextSoft,
                modifier = Modifier.size(SalpimDimens.LineIcon),
            )
            Text("오늘 운전 ${ui.report.score}점", style = SalpimType.BodyStrong, color = SalpimColors.Text)
        }
        ui.report.tips.firstOrNull()?.let {
            Text(
                it,
                style = SalpimType.Label,
                color = SalpimColors.TextSoft,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "운전 기록은 이 차 안에만 저장돼요",
            style = SalpimType.Caption,
            color = SalpimColors.TextSecondary,
            modifier = Modifier.weight(1f),
        )
        SecondaryButton("기록 지우기", onClick = actions::clearHistory, modifier = Modifier.testTag("btn_clear_history"))
    }
}
