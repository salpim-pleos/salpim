package com.salpim.app

import com.salpim.feature.fuel.RangeEstimate
import com.salpim.feature.fuel.RefuelAdvice
import com.salpim.feature.glance.FuelSummary
import kotlin.math.roundToInt

/** 주유 판단 → 화면·음성용 문장. 숫자는 10km 단위로 반올림해 "예상치"답게 보여 준다 */
object FuelText {
    private const val ROUND_KM = 10

    fun summary(advice: RefuelAdvice): FuelSummary? = when (advice) {
        RefuelAdvice.Unknown -> null
        is RefuelAdvice.Enough -> FuelSummary(rangeText(advice.range), needsRefuel = false)
        is RefuelAdvice.RefuelOnTheWay -> FuelSummary("가는 길에 주유", needsRefuel = true)
        is RefuelAdvice.RefuelNow -> FuelSummary("지금 주유", needsRefuel = true)
    }

    /** "190~330km" — 거의 확실(90%) ~ 아마도(75%) */
    fun rangeText(range: RangeEstimate): String = "${round(range.conservativeKm)}~${round(range.likelyKm)}km"

    private fun round(km: Double): Int = (km / ROUND_KM).roundToInt() * ROUND_KM
}
