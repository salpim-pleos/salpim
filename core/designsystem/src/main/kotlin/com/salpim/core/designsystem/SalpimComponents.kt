package com.salpim.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** 주 액션: 현대 네이비 직사각형 (docs/DESIGN.md 원칙 5). 나란히 놓을 때는 [centered]로 가운데 정렬 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    centered: Boolean = false,
) {
    ActionButton(
        text,
        onClick,
        modifier.background(SalpimColors.Action, RectangleShape),
        icon,
        centered,
        primary = true,
    )
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    centered: Boolean = false,
) {
    ActionButton(
        text,
        onClick,
        modifier.border(SalpimDimens.Line, SalpimColors.TextTertiary, RectangleShape),
        icon,
        centered,
        primary = false,
    )
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    icon: ImageVector?,
    centered: Boolean,
    primary: Boolean,
) {
    val color = if (primary) SalpimColors.OnAction else SalpimColors.Text
    Row(
        modifier
            .heightIn(min = if (primary) SalpimDimens.PrimaryButtonHeight else SalpimDimens.TouchMin)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centered) {
            Arrangement.spacedBy(SalpimDimens.ButtonIconGap, Alignment.CenterHorizontally)
        } else {
            Arrangement.spacedBy(SalpimDimens.ButtonIconGap)
        },
    ) {
        icon?.let {
            Icon(
                it,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(SalpimDimens.ButtonIcon),
            )
        }
        Text(
            text,
            style = if (primary) SalpimType.BodyStrong else SalpimType.LabelStrong,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 여러 할 일 중 하나를 고르는 칩. 고른 칩은 선택 색 면 + 굵은 글자 (색만으로 구분하지 않음) */
@Composable
fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Row(
        modifier
            .heightIn(min = SalpimDimens.TouchMin)
            .background(if (selected) SalpimColors.SurfaceRaised else SalpimColors.Surface, RectangleShape)
            .border(SalpimDimens.Line, if (selected) SalpimColors.Selected else SalpimColors.Track, RectangleShape)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SalpimDimens.SmallGap),
    ) {
        icon?.let { Icon(it, contentDescription = null, tint = SalpimColors.Text, modifier = Modifier.size(28.dp)) }
        Text(
            text,
            style = if (selected) SalpimType.LabelStrong else SalpimType.Label,
            color = if (selected) SalpimColors.Text else SalpimColors.TextSoft,
            maxLines = 1,
        )
    }
}

/** 패널 닫기 (터치 76dp) */
@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(SalpimDimens.TouchMin)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "닫기" }
            .testTag("btn_close_panel"),
        contentAlignment = Alignment.Center,
    ) {
        Icon(MdiIcons.Close, contentDescription = null, tint = SalpimColors.TextSoft, modifier = Modifier.size(36.dp))
    }
}

/** 시연 시나리오 한 줄 (시뮬레이션 메뉴용) */
data class SimulationItem(val id: String, val title: String, val playing: Boolean)

/**
 * 시뮬레이션 신호일 때 항상 보이는 표시 (AGENTS.md 2절).
 * [items]가 있고 [menuEnabled]면 눌러서 시연 시나리오를 고른다 — 주행 중에는 조작을 막으므로 표시만 한다.
 */
@Composable
fun SimulationBadge(
    items: List<SimulationItem>,
    menuEnabled: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val clickable = menuEnabled && items.isNotEmpty()
    Box(modifier) {
        Row(
            Modifier
                .heightIn(min = if (clickable) SalpimDimens.TouchMin else 0.dp)
                .then(if (clickable) Modifier.clickable(role = Role.Button) { open = true } else Modifier)
                .padding(horizontal = SalpimDimens.ItemGap)
                .testTag("badge_simulated"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("시뮬레이션 신호", style = SalpimType.Caption, color = SalpimColors.Simulated)
            if (clickable) {
                Icon(
                    MdiIcons.ChevronDown,
                    contentDescription = null,
                    tint = SalpimColors.Simulated,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        DropdownMenu(expanded = open && clickable, onDismissRequest = { open = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(if (item.playing) "재생 중: ${item.title}" else item.title, style = SalpimType.Label) },
                    onClick = {
                        open = false
                        onSelect(item.id)
                    },
                    modifier = Modifier.heightIn(min = SalpimDimens.TouchMin).testTag("scenario_${item.id}"),
                )
            }
        }
    }
}
