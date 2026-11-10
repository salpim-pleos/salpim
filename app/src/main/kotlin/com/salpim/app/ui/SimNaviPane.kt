package com.salpim.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import com.salpim.app.MainUiState
import com.salpim.core.designsystem.MdiIcons
import com.salpim.core.designsystem.SalpimColors
import com.salpim.core.designsystem.SalpimDimens
import com.salpim.core.designsystem.SalpimType

/** 주행 1km당 지도 이동 픽셀 (움직이는 느낌만, 축척 아님) [설계] */
private const val PX_PER_KM = 6_000.0
private const val BLOCK_PX = 220f
private const val SIDE_ROAD_PX = 16f
private const val MAIN_ROAD_PX = 56f
private const val ROUTE_PX = 18f
private const val CAR_Y_FRACTION = 0.74f
private const val CAR_RADIUS_PX = 22f
private const val ARROW_PX = 14f
private const val WAYPOINT_Y_FRACTION = 0.32f
private const val WAYPOINT_RADIUS_PX = 18f

/**
 * 시뮬레이션에서만 보이는 "시스템 내비" 자리의 그림. 실제 차에서는 이 자리에 Pleos 내비가 뜨고 살핌은 옆에 붙는다.
 * 지도 데이터가 아니라 길 모양을 그려 차가 달리는 느낌만 준다 (외부 지도 API 없음). 시연용이라고 늘 표시한다.
 */
@Composable
fun SimNaviPane(ui: MainUiState, modifier: Modifier = Modifier) {
    // 프레임마다 도는 애니메이션 대신 신호(주행거리)가 바뀔 때만 다시 그린다. 화면 테스트가 멈춘 상태를 기다릴 수 있다
    val offsetPx = ((ui.odometerKm ?: 0.0) * PX_PER_KM % BLOCK_PX).toFloat()
    Box(modifier.background(SalpimColors.NaviBackground).testTag("sim_navi")) {
        Canvas(Modifier.fillMaxSize()) {
            drawMap(offsetPx, hasRoute = ui.destinationName != null, hasWaypoint = ui.inputs.waypoint != null)
        }
        RouteInfo(ui, Modifier.align(Alignment.TopStart).padding(SalpimDimens.ScreenPadding))
        Text(
            "내비게이션 자리 · 시연용 그림",
            style = SalpimType.Caption,
            color = SalpimColors.TextSecondary,
            modifier = Modifier.align(Alignment.BottomStart).padding(SalpimDimens.ScreenPadding),
        )
    }
}

@Composable
private fun RouteInfo(ui: MainUiState, modifier: Modifier) {
    val destination = ui.destinationName ?: return
    Column(
        modifier.background(SalpimColors.Surface).padding(SalpimDimens.ItemGap),
        verticalArrangement = Arrangement.spacedBy(SalpimDimens.SmallGap),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SalpimDimens.SmallGap),
        ) {
            Icon(
                MdiIcons.Navigation,
                contentDescription = null,
                tint = SalpimColors.Notice,
                modifier = Modifier.size(SalpimDimens.SmallIcon),
            )
            Text(destination, style = SalpimType.BodyStrong, color = SalpimColors.Text)
            Text(
                ui.route?.let { "%.1fkm".format(it.remainingKm) } ?: "경로 찾는 중",
                style = SalpimType.Label,
                color = SalpimColors.TextSoft,
            )
        }
        ui.inputs.waypoint?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SalpimDimens.SmallGap),
            ) {
                Icon(
                    MdiIcons.GasStation,
                    contentDescription = null,
                    tint = SalpimColors.Caution,
                    modifier = Modifier.size(SalpimDimens.SmallIcon),
                )
                Text("경유 $it", style = SalpimType.Label, color = SalpimColors.Caution)
            }
        }
    }
}

/** 옆길은 아래로 흘러가고, 가운데 큰길 위에 경로(파랑)와 내 차(흰 원 + 화살표) */
private fun DrawScope.drawMap(offsetPx: Float, hasRoute: Boolean, hasWaypoint: Boolean) {
    val w = size.width
    val h = size.height
    var y = offsetPx % BLOCK_PX - BLOCK_PX
    while (y < h + BLOCK_PX) {
        drawLine(SalpimColors.NaviRoad, Offset(0f, y), Offset(w, y + BLOCK_PX * SIDE_ROAD_SLOPE), SIDE_ROAD_PX)
        y += BLOCK_PX
    }
    val roadX = w / 2
    drawLine(SalpimColors.NaviRoadEdge, Offset(roadX, 0f), Offset(roadX, h), MAIN_ROAD_PX + ROAD_EDGE_PX)
    drawLine(SalpimColors.NaviRoad, Offset(roadX, 0f), Offset(roadX, h), MAIN_ROAD_PX)
    val carY = h * CAR_Y_FRACTION
    if (hasRoute) drawLine(SalpimColors.Notice, Offset(roadX, 0f), Offset(roadX, carY), ROUTE_PX, StrokeCap.Round)
    if (hasWaypoint) {
        val pin = Offset(roadX, h * WAYPOINT_Y_FRACTION)
        drawCircle(SalpimColors.Caution, WAYPOINT_RADIUS_PX, pin)
        drawCircle(SalpimColors.NaviBackground, WAYPOINT_RADIUS_PX * WAYPOINT_HOLE, pin)
    }
    drawCircle(Color.White, CAR_RADIUS_PX, Offset(roadX, carY))
    drawPath(carArrow(roadX, carY), SalpimColors.Action)
}

/** 위를 향한 화살촉 (진행 방향) */
private fun carArrow(x: Float, y: Float) = Path().apply {
    moveTo(x, y - ARROW_PX)
    lineTo(x + ARROW_PX * ARROW_HALF_WIDTH, y + ARROW_PX * ARROW_BACK)
    lineTo(x, y + ARROW_PX * ARROW_NOTCH)
    lineTo(x - ARROW_PX * ARROW_HALF_WIDTH, y + ARROW_PX * ARROW_BACK)
    close()
}

private const val SIDE_ROAD_SLOPE = 0.25f
private const val ROAD_EDGE_PX = 6f
private const val WAYPOINT_HOLE = 0.45f
private const val ARROW_HALF_WIDTH = 0.8f
private const val ARROW_BACK = 0.7f
private const val ARROW_NOTCH = 0.3f
