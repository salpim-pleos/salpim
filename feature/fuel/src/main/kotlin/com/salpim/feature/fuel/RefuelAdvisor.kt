package com.salpim.feature.fuel

/** 주유 판단 결과 */
sealed interface RefuelAdvice {
    /** 연료 잔량을 모른다 — 아무것도 표시하지 않는다 */
    data object Unknown : RefuelAdvice

    /** 90% 경우에도 충분 */
    data class Enough(val range: RangeEstimate) : RefuelAdvice

    /** 75% 경우엔 충분하지만 90%는 못 덮음 → 가는 길에 주유 권장 */
    data class RefuelOnTheWay(val range: RangeEstimate, val neededKm: Double) : RefuelAdvice

    /** 75% 경우도 못 덮음, 또는 남은 거리가 위험 수준 → 지금 가까운 주유소 */
    data class RefuelNow(val range: RangeEstimate, val neededKm: Double) : RefuelAdvice

    val showsStations: Boolean get() = this is RefuelOnTheWay || this is RefuelNow
}

data class RefuelConfig(
    /** 목적지에 도착해서도 남겨 둘 여유 거리 [설계] */
    val reserveKm: Double = 30.0,
    /** 보수적 남은 거리가 이보다 작으면 목적지와 상관없이 지금 주유 [설계] */
    val criticalKm: Double = 40.0,
)

/**
 * 주유 결정 트리. 필요 거리 = 목적지까지 남은 거리(없으면 0) + 여유 거리.
 *
 * ```
 * 잔량 모름 ─────────────────────────────────────── Unknown
 * 보수적(90%) 거리 < 위험 거리 ─────────────────── RefuelNow
 * 보수적(90%) 거리 ≥ 필요 거리 ─────────────────── Enough
 * 예상(75%) 거리 ≥ 필요 거리 (90%는 부족) ──────── RefuelOnTheWay
 * 그 외 (75%도 부족) ────────────────────────────── RefuelNow
 * ```
 */
class RefuelAdvisor(
    private val estimator: RangeEstimator = RangeEstimator(),
    private val config: RefuelConfig = RefuelConfig(),
) {
    fun advise(fuelPercent: Double?, kmPerPercentSamples: List<Double>, destinationRemainingKm: Double?): RefuelAdvice {
        if (fuelPercent == null) return RefuelAdvice.Unknown
        val range = estimator.estimate(fuelPercent, kmPerPercentSamples)
        val neededKm = (destinationRemainingKm ?: 0.0) + config.reserveKm
        return when {
            range.conservativeKm < config.criticalKm -> RefuelAdvice.RefuelNow(range, neededKm)
            range.conservativeKm >= neededKm -> RefuelAdvice.Enough(range)
            range.likelyKm >= neededKm -> RefuelAdvice.RefuelOnTheWay(range, neededKm)
            else -> RefuelAdvice.RefuelNow(range, neededKm)
        }
    }
}
