package com.salpim.core.designsystem

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salpim.core.model.CarZone

/** 경고 점등: 처음 켜질 때 한 번만 밝아진다. 반복 깜빡임 없음 (docs/DESIGN.md) */
private const val LIGHT_ON_MS = 600

/** 차 그림 비율 (가로:세로 = 150:270) */
private const val CAR_ASPECT = 270f / 150f

/**
 * 위에서 본 내 차. [litZone]만 [litColor]로 칠하고 빛 번짐을 준다.
 * 시스템 "애니메이션 줄이기"(animator duration scale 0)면 바로 켜진다.
 */
@Composable
fun CarTopView(
    litZone: CarZone?,
    litColor: Color,
    description: String,
    modifier: Modifier = Modifier,
    width: Dp = 150.dp,
) {
    val brightness = remember { Animatable(0f) }
    val reduceMotion = rememberReduceMotion()
    LaunchedEffect(litZone, litColor) {
        if (litZone == null || reduceMotion) {
            brightness.snapTo(1f)
        } else {
            brightness.snapTo(0f)
            brightness.animateTo(1f, tween(LIGHT_ON_MS, easing = FastOutSlowInEasing))
        }
    }
    Canvas(
        modifier
            .size(width, width * CAR_ASPECT)
            .semantics { contentDescription = description }
            .testTag("car_view"),
    ) {
        drawCarBody()
        litZone?.let { drawLitZone(it, litColor, brightness.value) }
    }
}

@Composable
private fun rememberReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember { Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
}

/** 차 좌표계: 가로 150, 세로 270 단위 */
private fun DrawScope.unit(): Float = size.width / CarGeometry.WIDTH

private fun DrawScope.drawCarBody() {
    val u = unit()
    CarGeometry.wheels.forEach { (x, y) ->
        drawRoundRect(
            SalpimColors.Track,
            Offset(x * u, y * u),
            Size(CarGeometry.WHEEL_W * u, CarGeometry.WHEEL_H * u),
            CornerRadius(CarGeometry.WHEEL_CORNER * u),
        )
    }
    val body = CarGeometry.body
    drawRoundRect(
        SalpimColors.CarBody,
        Offset(body.left * u, body.top * u),
        Size(body.width * u, body.height * u),
        CornerRadius(CarGeometry.BODY_CORNER * u),
    )
    drawRoundRect(
        SalpimColors.TextTertiary,
        Offset(body.left * u, body.top * u),
        Size(body.width * u, body.height * u),
        CornerRadius(CarGeometry.BODY_CORNER * u),
        style = Stroke(width = CarGeometry.OUTLINE * u),
    )
    CarGeometry.glass.forEach { r ->
        drawRoundRect(
            SalpimColors.Track,
            Offset(r.left * u, r.top * u),
            Size(r.width * u, r.height * u),
            CornerRadius(CarGeometry.GLASS_CORNER * u),
        )
    }
}

private fun DrawScope.drawLitZone(zone: CarZone, color: Color, brightness: Float) {
    val u = unit()
    CarGeometry.zoneRects(zone).forEach { r ->
        val topLeft = Offset(r.left * u, r.top * u)
        val area = Size(r.width * u, r.height * u)
        val glowPad = CarGeometry.GLOW_PAD * u
        drawRoundRect(
            color.copy(alpha = CarGeometry.GLOW_ALPHA * brightness),
            topLeft - Offset(glowPad, glowPad),
            Size(area.width + glowPad + glowPad, area.height + glowPad + glowPad),
            CornerRadius(CarGeometry.GLOW_CORNER * u),
        )
        drawRoundRect(color.copy(alpha = brightness), topLeft, area, CornerRadius(CarGeometry.ZONE_CORNER * u))
    }
}

private data class CarRect(val left: Float, val top: Float, val width: Float, val height: Float)

/** 그림 좌표 (캔버스 시안과 같은 값) */
private object CarGeometry {
    const val WIDTH = 150f
    const val WHEEL_W = 14f
    const val WHEEL_H = 44f
    const val GLOW_PAD = 10f
    const val GLOW_ALPHA = 0.3f
    const val WHEEL_CORNER = 4f
    const val BODY_CORNER = 26f
    const val GLASS_CORNER = 8f
    const val GLOW_CORNER = 16f
    const val ZONE_CORNER = 10f
    const val OUTLINE = 3f

    val wheels = listOf(18f to 34f, 118f to 34f, 18f to 190f, 118f to 190f)
    val body = CarRect(22f, 6f, 106f, 258f)
    val glass = listOf(CarRect(38f, 80f, 74f, 26f), CarRect(40f, 196f, 70f, 24f))

    private val frontWheels = wheels.take(2).map { (x, y) -> CarRect(x, y, WHEEL_W, WHEEL_H) }

    /** 위치별로 칠할 영역 */
    private val zones: Map<CarZone, List<CarRect>> = mapOf(
        CarZone.ENGINE to listOf(CarRect(26f, 10f, 98f, 62f)),
        CarZone.BRAKE to frontWheels,
        CarZone.TRANSMISSION to listOf(CarRect(66f, 112f, 18f, 70f)),
        CarZone.WASHER to listOf(CarRect(38f, 80f, 74f, 26f)),
        CarZone.SEAT_BELT to listOf(CarRect(44f, 124f, 26f, 30f), CarRect(80f, 124f, 26f, 30f)),
        CarZone.AIRBAG to listOf(CarRect(40f, 108f, 70f, 14f)),
    )

    fun zoneRects(zone: CarZone): List<CarRect> = zones.getValue(zone)
}
