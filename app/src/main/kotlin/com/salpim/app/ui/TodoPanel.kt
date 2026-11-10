package com.salpim.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.salpim.app.MainUiState
import com.salpim.app.PanelMode
import com.salpim.app.Todo
import com.salpim.core.designsystem.ChoiceChip
import com.salpim.core.designsystem.CloseButton
import com.salpim.core.designsystem.MdiIcons
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.designsystem.SalpimDimens
import com.salpim.core.designsystem.SalpimType
import com.salpim.core.designsystem.SecondaryButton
import com.salpim.feature.service.NearbyPlace
import com.salpim.feature.service.PlaceCategory

/**
 * 정차 화면 오른쪽: 한 번에 한 가지만, 스크롤 없이. 위는 머리줄(제목 또는 할 일 칩, 닫기),
 * 가운데는 내용, 버튼은 늘 패널 맨 아래에 맞춘다.
 */
@Composable
fun TodoPanel(ui: MainUiState, actions: MainActions, modifier: Modifier) {
    Column(
        modifier.background(SalpimColors.Surface).padding(SalpimDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap),
    ) {
        when (val panel = ui.panel) {
            PanelMode.Places -> PlacesPanel(ui, actions)
            PanelMode.Reservation -> ReservationPanel(actions)
            is PanelMode.Doing -> DoingPanel(panel, ui, actions)
            PanelMode.Nothing -> NothingPanel(ui, actions)
        }
    }
}

/** 머리줄: 높이를 터치 크기로 고정해 패널마다 내용 시작선이 같다 */
@Composable
internal fun PanelHeader(
    title: String,
    caption: String? = null,
    onClose: (() -> Unit)? = null,
    captionTag: String? = null,
) {
    Row(Modifier.fillMaxWidth().heightIn(min = SalpimDimens.TouchMin), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = SalpimType.TitleSmall,
                color = SalpimColors.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            caption?.let {
                Text(
                    it,
                    style = SalpimType.Caption,
                    color = SalpimColors.TextSecondary,
                    maxLines = 1,
                    modifier = captionTag?.let { tag -> Modifier.testTag(tag) } ?: Modifier,
                )
            }
        }
        onClose?.let { CloseButton(it) }
    }
}

@Composable
private fun ColumnScope.DoingPanel(panel: PanelMode.Doing, ui: MainUiState, actions: MainActions) {
    if (panel.all.size > 1) {
        TodoChips(panel, actions)
    } else {
        PanelHeader("지금 할 일")
    }
    when (val todo = panel.todo) {
        is Todo.Service -> ServiceContent(todo, actions)
        is Todo.Refuel -> RefuelContent(todo, ui, actions)
    }
}

@Composable
private fun TodoChips(panel: PanelMode.Doing, actions: MainActions) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SalpimDimens.SmallGap)) {
        panel.all.forEach { todo ->
            val (label, icon) = when (todo) {
                is Todo.Service -> todo.explanation.title to todo.explanation.icon()
                is Todo.Refuel -> "주유" to MdiIcons.GasStation
            }
            ChoiceChip(
                label,
                selected = todo.key == panel.todo.key,
                onClick = { actions.selectTodo(todo.key) },
                icon = icon,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}

/** 장소 목록 한 줄 높이 = 터치 크기. 3곳이 머리줄 아래에 스크롤 없이 들어간다 */
@Composable
private fun ColumnScope.PlacesPanel(ui: MainUiState, actions: MainActions) {
    val places = ui.inputs.places.orEmpty()
    val what = if (ui.inputs.placesCategory == PlaceCategory.GAS_STATION) "주유소" else "블루핸즈"
    PanelHeader(
        title = "가까운 $what ${places.size}곳",
        caption = if (ui.inputs.placesSample) "샘플 목록 · 서울시청 기준 직선거리" else null,
        onClose = actions::closePanel,
        captionTag = "badge_sample",
    )
    Column(
        Modifier.weight(1f).testTag("places_list"),
        // 줄 간격을 좁혀 3곳 + 안내 문구가 스크롤 없이 들어가게
        verticalArrangement = Arrangement.spacedBy(SalpimDimens.SmallGap / 2),
    ) {
        places.forEachIndexed { index, place -> PlaceRow(index + 1, place, actions) }
        Spacer(Modifier.weight(1f))
        ui.inputs.navMessage?.let {
            Text(
                it,
                style = SalpimType.Caption,
                color = SalpimColors.TextSoft,
                maxLines = 1,
                modifier = Modifier.testTag("nav_message"),
            )
        }
    }
}

@Composable
private fun PlaceRow(ordinal: Int, place: NearbyPlace, actions: MainActions) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = SalpimDimens.TouchMin),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap),
    ) {
        Box(
            Modifier.size(SalpimDimens.LineIcon).border(SalpimDimens.Line, SalpimColors.TextTertiary, RectangleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("$ordinal", style = SalpimType.BodyStrong, color = SalpimColors.Text)
        }
        Column(Modifier.weight(1f)) {
            Text(
                place.name,
                style = SalpimType.LabelStrong,
                color = SalpimColors.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val distance = place.distanceKm?.let { "%.1fkm".format(it) }
            Text(
                listOfNotNull(distance, place.address).joinToString(" · "),
                style = SalpimType.Caption,
                color = SalpimColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SecondaryButton(
            "길 안내",
            onClick = { actions.routeTo(place) },
            icon = MdiIcons.Navigation,
            modifier = Modifier.testTag("btn_route_${place.id}"),
        )
    }
}

/** 칩이 없을 때도 빈 공간 대신 아이콘 + 상태를 보여 준다 */
@Composable
internal fun PanelIconTitle(icon: ImageVector, tint: Color, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SalpimDimens.ItemGap),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(SalpimDimens.LineIcon))
        Text(
            title,
            style = SalpimType.TitleSmall,
            color = SalpimColors.Text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
