package com.salpim.core.signal.sim

import com.salpim.core.model.VehicleSignal
import kotlin.math.abs

/**
 * 시뮬레이션 차량의 주행거리·연료·경로 남은 거리를 시간에 따라 진행시킨다. [설계]
 * 연비는 속도에 따라 달라지게 해서 주유 예측의 분포(분위수)가 의미 있게 만든다.
 */
internal object SimulatedVehicleModel {
    /** 가장 효율적인 속도에서 연료 1%당 km */
    private const val BEST_KM_PER_PERCENT = 7.0

    /** 최적 속도에서 이만큼 벗어날 때마다 연료 1%당 1km씩 나빠진다 */
    private const val EFFICIENCY_DROP_PER_KMH = 1.0 / 40.0
    private const val MIN_KM_PER_PERCENT = 3.0
    private const val BEST_SPEED_KMH = 70.0
    private const val MS_PER_HOUR = 3_600_000.0

    fun advance(state: VehicleSignal, elapsedMs: Long): VehicleSignal {
        val speedKmh = state.speedKmh?.toDouble() ?: return state
        val drivenKm = speedKmh * elapsedMs / MS_PER_HOUR
        if (drivenKm <= 0.0) return state
        return state.copy(
            odometerKm = state.odometerKm?.plus(drivenKm),
            fuelPercent = state.fuelPercent?.let { (it - drivenKm / kmPerPercentAt(speedKmh)).coerceAtLeast(0.0) },
            routeRemainingKm = state.routeRemainingKm?.let { (it - drivenKm).coerceAtLeast(0.0) },
        )
    }

    fun kmPerPercentAt(speedKmh: Double): Double =
        (BEST_KM_PER_PERCENT - abs(speedKmh - BEST_SPEED_KMH) * EFFICIENCY_DROP_PER_KMH).coerceAtLeast(
            MIN_KM_PER_PERCENT,
        )
}
