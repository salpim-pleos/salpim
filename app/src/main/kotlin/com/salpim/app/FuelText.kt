package com.salpim.app

import com.salpim.core.model.VehicleSignal
import com.salpim.feature.fuel.RangeEstimate
import com.salpim.feature.fuel.RefuelAdvice
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 남은 거리 막대 비율 (0..1). 진한 부분 = 거의 확실(90%), 옅은 부분 = 아마도(75%).
 * [needFraction]은 목적지 + 여유 거리 위치 (모르면 null).
 */
data class FuelBar(val conservativeFraction: Float, val likelyFraction: Float, val needFraction: Float?)

/** 목적지와 남은 거리. 주유 판단(필요 거리)의 근거라 연료 줄에 함께 보여 준다 */
data class RouteProgress(val destinationName: String, val remainingKm: Double) {
    /** "서울시청까지 5.9km" — 10km 미만은 소수 한 자리 */
    val text: String
        get() = "${destinationName}까지 ${formatKm(remainingKm)}"

    companion object {
        private const val ONE_DECIMAL_BELOW_KM = 10.0

        fun from(signal: VehicleSignal?): RouteProgress? {
            val name = signal?.destinationName ?: return null
            val km = signal.routeRemainingKm ?: return null
            return RouteProgress(name, km)
        }

        private fun formatKm(km: Double): String =
            if (km < ONE_DECIMAL_BELOW_KM) "%.1fkm".format(km) else "${km.roundToInt()}km"
    }
}

/** 주유 예측 요약: 화면 한 줄 [text], 막대 옆 짧은 숫자 [rangeText], 막대 [bar] */
data class FuelSummary(val text: String, val rangeText: String, val bar: FuelBar, val needsRefuel: Boolean)

/** 주유 판단 → 화면·음성용 문장. 숫자는 10km 단위로 반올림해 "예상치"답게 보여 준다 */
object FuelText {
    private const val ROUND_KM = 10

    /** 막대 끝까지 채우지 않는다. 예측이 "가득"처럼 보이지 않게 [설계] */
    private const val BAR_HEADROOM = 0.8

    fun summary(advice: RefuelAdvice): FuelSummary? = when (advice) {
        RefuelAdvice.Unknown -> null
        is RefuelAdvice.Enough -> summary(
            "연료로 ${rangeText(advice.range)} 더 갈 수 있어요",
            advice.range,
            null,
            needsRefuel = false,
        )
        is RefuelAdvice.RefuelOnTheWay ->
            summary("가는 길에 주유하세요 (${rangeText(advice.range)})", advice.range, advice.neededKm, needsRefuel = true)
        is RefuelAdvice.RefuelNow ->
            summary("지금 주유하세요 (${rangeText(advice.range)})", advice.range, advice.neededKm, needsRefuel = true)
    }

    /** "190~330km" — 거의 확실(90%) ~ 아마도(75%) */
    fun rangeText(range: RangeEstimate): String = "${round(range.conservativeKm)}~${round(range.likelyKm)}km"

    /** 막대 눈금은 예상 거리와 필요 거리 중 큰 쪽을 [BAR_HEADROOM] 위치에 둔다 */
    fun bar(range: RangeEstimate, neededKm: Double?): FuelBar {
        val scaleKm = max(range.likelyKm, neededKm ?: 0.0) / BAR_HEADROOM
        if (scaleKm <= 0.0) return FuelBar(0f, 0f, null)
        return FuelBar(
            conservativeFraction = (range.conservativeKm / scaleKm).toFloat(),
            likelyFraction = (range.likelyKm / scaleKm).toFloat(),
            needFraction = neededKm?.let { (it / scaleKm).toFloat() },
        )
    }

    private fun summary(text: String, range: RangeEstimate, neededKm: Double?, needsRefuel: Boolean) =
        FuelSummary(text, rangeText(range), bar(range, neededKm), needsRefuel)

    private fun round(km: Double): Int = (km / ROUND_KM).roundToInt() * ROUND_KM
}
