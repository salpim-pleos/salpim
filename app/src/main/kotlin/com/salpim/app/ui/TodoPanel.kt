package com.salpim.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.salpim.app.MainUiState
import com.salpim.core.designsystem.PrimaryButton
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.designsystem.SalpimDimens
import com.salpim.core.designsystem.SalpimType
import com.salpim.core.designsystem.SecondaryButton
import com.salpim.core.model.Explanation
import com.salpim.feature.service.NearbyPlace
import com.salpim.feature.service.PlaceCategory
import com.salpim.feature.service.ReservationLink

private const val QR_SIZE_DP = 200

/** 정차 화면 오른쪽: "지금 할 일" 하나를 크게, 그 일에 바로 이어지는 버튼 */
@Composable
fun TodoPanel(ui: MainUiState, actions: MainActions, modifier: Modifier) {
    Column(
        modifier.background(
            SalpimColors.Surface,
        ).verticalScroll(rememberScrollState()).padding(SalpimDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap),
    ) {
        Text("지금 할 일", style = SalpimType.Label, color = SalpimColors.TextSecondary)
        ui.selected?.let { WarningTodo(it, actions) } ?: NothingToDo()
        if (ui.fuel?.needsRefuel == true) {
            Text(ui.fuel.text, style = SalpimType.BodyStrong, color = SalpimColors.Caution)
            PrimaryButton(
                "가까운 주유소",
                onClick = { actions.findNearby(PlaceCategory.GAS_STATION) },
                modifier = Modifier.testTag("btn_find_gas"),
            )
        }
        ui.inputs.places?.let { PlaceList(it, ui.inputs.placesMock, actions) }
        if (ui.inputs.showReservation) ReservationPanel()
        ui.inputs.navMessage?.let {
            Text(
                it,
                style = SalpimType.Label,
                color = SalpimColors.Text,
                modifier = Modifier.testTag("nav_message"),
            )
        }
        Divider()
        ReportLine(ui, actions)
    }
}

@Composable
private fun WarningTodo(e: Explanation, actions: MainActions) {
    Column(Modifier.testTag("warning_detail"), verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap)) {
        Text(e.oneLine.substringBefore(". "), style = SalpimType.TitleSmall, color = SalpimColors.Text)
        Text(e.detail, style = SalpimType.Label, color = SalpimColors.TextSoft)
        e.actions.forEachIndexed { i, action ->
            Text(
                "${i + 1}  $action",
                style = SalpimType.Label,
                color = SalpimColors.Text,
            )
        }
        if (e.needsService) {
            PrimaryButton(
                "가까운 블루핸즈로 안내",
                onClick = { actions.findNearby(PlaceCategory.BLUE_HANDS) },
                modifier = Modifier.testTag("btn_navigate_service"),
            )
            SecondaryButton(
                "휴대폰으로 예약",
                onClick = actions::showReservation,
                modifier = Modifier.testTag("btn_reservation"),
            )
        }
        Text("정확한 상태는 정비사가 확인해요. 이 안내는 참고용이에요.", style = SalpimType.Caption, color = SalpimColors.TextSecondary)
    }
}

@Composable
private fun NothingToDo() {
    Text("지금은 할 일이 없어요", style = SalpimType.TitleSmall, color = SalpimColors.Text)
}

@Composable
private fun PlaceList(places: List<NearbyPlace>, mock: Boolean, actions: MainActions) {
    Column(Modifier.testTag("places_list"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (mock) MockDataLabel()
        places.forEachIndexed { index, place -> PlaceRow(index + 1, place, actions) }
    }
}

@Composable
private fun MockDataLabel() {
    Text("예시 데이터", style = SalpimType.Caption, color = SalpimColors.TextSoft, modifier = Modifier.testTag("badge_mock"))
}

@Composable
private fun PlaceRow(ordinal: Int, place: NearbyPlace, actions: MainActions) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = SalpimDimens.ListRowHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier.size(48.dp).border(SalpimDimens.Line, SalpimColors.TextTertiary, RectangleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("$ordinal", style = SalpimType.BodyStrong, color = SalpimColors.Text)
        }
        Column(Modifier.weight(1f)) {
            Text(place.name, style = SalpimType.LabelStrong, color = SalpimColors.Text)
            val distance = place.distanceKm?.let { "%.1fkm".format(it) }
            Text(
                listOfNotNull(distance, place.address).joinToString(", "),
                style = SalpimType.Caption,
                color = SalpimColors.TextSecondary,
            )
        }
        SecondaryButton(
            "길 안내",
            onClick = { actions.routeTo(place) },
            modifier = Modifier.testTag("btn_route_${place.id}"),
        )
    }
}

/** 휴대폰으로 예약: 공식 예약 페이지 QR. TODO(qr): ZXing으로 실제 QR 렌더링 */
@Composable
private fun ReservationPanel() {
    Column(Modifier.testTag("reservation_panel"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("휴대폰으로 예약", style = SalpimType.TitleSmall, color = SalpimColors.Text)
        Text(
            "카메라로 찍으면 현대 공식 정비 예약 페이지가 열려요. 예약은 3일 뒤부터 할 수 있어요.",
            style = SalpimType.Label,
            color = SalpimColors.TextSoft,
        )
        Box(Modifier.size(QR_SIZE_DP.dp).background(SalpimColors.Text), contentAlignment = Alignment.Center) {
            Text("QR 준비 중", style = SalpimType.Caption, color = SalpimColors.Background)
        }
        Text(ReservationLink.OFFICIAL_RESERVATION_URL, style = SalpimType.Caption, color = SalpimColors.TextSecondary)
    }
}

@Composable
private fun ReportLine(ui: MainUiState, actions: MainActions) {
    Text(
        "오늘 운전 ${ui.report.score}점",
        style = SalpimType.BodyStrong,
        color = SalpimColors.Text,
        modifier = Modifier.testTag("coach_report"),
    )
    ui.report.tips.forEach { Text(it, style = SalpimType.Label, color = SalpimColors.TextSoft) }
    Text("운전 기록은 이 차 안에만 저장돼요", style = SalpimType.Caption, color = SalpimColors.TextSecondary)
    SecondaryButton(
        "기록 지우기",
        onClick = actions::clearHistory,
        modifier = Modifier.fillMaxWidth(0.5f).testTag("btn_clear_history"),
    )
}
