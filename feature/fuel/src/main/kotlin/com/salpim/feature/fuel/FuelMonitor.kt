package com.salpim.feature.fuel

/**
 * 신호가 들어올 때마다 연비 기록을 쌓고 주유 판단을 내린다. 엔진은 이 클래스 하나만 본다.
 */
class FuelMonitor(
    private val tracker: EfficiencyTracker = EfficiencyTracker(),
    private val advisor: RefuelAdvisor = RefuelAdvisor(),
) {
    fun onReading(odometerKm: Double?, fuelPercent: Double?, routeRemainingKm: Double?): RefuelAdvice {
        if (odometerKm != null && fuelPercent != null) tracker.record(FuelReading(odometerKm, fuelPercent))
        return advisor.advise(fuelPercent, tracker.kmPerPercentSamples, routeRemainingKm)
    }
}
