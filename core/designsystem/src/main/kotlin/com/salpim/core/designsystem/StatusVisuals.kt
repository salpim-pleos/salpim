package com.salpim.core.designsystem

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp

/** 경고 점등: 처음 켜질 때 한 번만 밝아진다. 반복 깜빡임 없음 (docs/DESIGN.md) */
private const val LIGHT_ON_MS = 600

/** 켜진 아이콘 뒤 면의 투명도. 색만 살짝 비치게 */
private const val LIT_FILL_ALPHA = 0.14f

/**
 * 상태 아이콘. [litColor]가 있으면 그 색으로 켜지고(뒤 면에 옅은 색) 한 번 밝아진다.
 * 시스템 "애니메이션 줄이기"(animator duration scale 0)면 바로 켜진다.
 */
@Composable
fun StatusIcon(
    icon: ImageVector,
    description: String?,
    litColor: Color?,
    modifier: Modifier = Modifier,
    boxSize: Dp = SalpimDimens.HeroIconBox,
    iconSize: Dp = SalpimDimens.HeroIcon,
) {
    val brightness = remember { Animatable(0f) }
    val reduceMotion = rememberReduceMotion()
    LaunchedEffect(icon, litColor) {
        if (litColor == null || reduceMotion) {
            brightness.snapTo(1f)
        } else {
            brightness.snapTo(0f)
            brightness.animateTo(1f, tween(LIGHT_ON_MS, easing = FastOutSlowInEasing))
        }
    }
    val described = description?.let { d -> Modifier.semantics { contentDescription = d } } ?: Modifier
    Box(
        modifier
            .size(boxSize)
            .then(described)
            .background(litColor?.copy(alpha = LIT_FILL_ALPHA * brightness.value) ?: Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = litColor ?: SalpimColors.Text,
            modifier = Modifier.size(iconSize).alpha(brightness.value),
        )
    }
}

/**
 * 남은 거리 막대. 진한 부분 = 거의 확실(90%), 옅은 부분 = 아마도(75%)까지, 세로 눈금 = 필요한 거리.
 * 비율은 0..1, [likelyFraction] ≥ [conservativeFraction].
 */
@Composable
fun RangeBar(
    conservativeFraction: Float,
    likelyFraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    needFraction: Float? = null,
) {
    Box(modifier.fillMaxWidth().height(SalpimDimens.BarHeight).background(SalpimColors.SurfaceRaised)) {
        Box(Modifier.barFill(likelyFraction).background(SalpimColors.TextTertiary))
        Box(Modifier.barFill(conservativeFraction).background(color))
        needFraction?.let { need ->
            Box(Modifier.barFill(need)) {
                val marker = Modifier.align(Alignment.CenterEnd).width(SalpimDimens.Line * 2)
                Box(marker.fillMaxHeight().background(SalpimColors.Text))
            }
        }
    }
}

private fun Modifier.barFill(fraction: Float): Modifier = fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f))

@Composable
private fun rememberReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember { Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
}
