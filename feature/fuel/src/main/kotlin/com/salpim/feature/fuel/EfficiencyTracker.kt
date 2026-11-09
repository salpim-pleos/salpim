package com.salpim.feature.fuel

/** 누적 주행거리와 연료 잔량 한 쌍. 출처: Odometer(`CAR_DRIVE`), Fuel(`CAR_ENERGY`) — TODO(spike-S6): 메서드 확인 */
data class FuelReading(val odometerKm: Double, val fuelPercent: Double)

data class EfficiencyConfig(
    /** 이만큼 연료가 줄어야 연비 샘플 하나로 인정 (게이지 잡음 제거) */
    val minSegmentPercent: Double = 2.0,
    /** 연료가 이만큼 늘면 주유로 보고 구간을 새로 시작 */
    val refuelJumpPercent: Double = 3.0,
    /** 보관할 최근 샘플 수 */
    val maxSamples: Int = 200,
)

/**
 * 주행 기록에서 "연료 1%당 주행 km" 샘플을 모은다. [설계]
 * 연비를 L/100km가 아니라 %당 km로 재는 이유: 탱크 용량을 몰라도 잔량(%)만으로 남은 거리를 바로 계산할 수 있다.
 * TODO(datastore): 샘플 영속화 (현재 메모리)
 */
class EfficiencyTracker(
    private val config: EfficiencyConfig = EfficiencyConfig(),
    initialKmPerPercent: List<Double> = emptyList(),
) {
    private var segmentStart: FuelReading? = null
    private val samples = ArrayDeque(initialKmPerPercent.takeLast(config.maxSamples))

    val kmPerPercentSamples: List<Double> get() = samples.toList()

    fun record(reading: FuelReading) {
        val start = segmentStart
        if (start == null) {
            segmentStart = reading
            return
        }
        val usedPercent = start.fuelPercent - reading.fuelPercent
        when {
            usedPercent <= -config.refuelJumpPercent -> segmentStart = reading
            usedPercent >= config.minSegmentPercent -> closeSegment(start, reading, usedPercent)
        }
    }

    private fun closeSegment(start: FuelReading, end: FuelReading, usedPercent: Double) {
        val drivenKm = end.odometerKm - start.odometerKm
        if (drivenKm > 0) addSample(drivenKm / usedPercent)
        segmentStart = end
    }

    private fun addSample(kmPerPercent: Double) {
        samples.addLast(kmPerPercent)
        while (samples.size > config.maxSamples) samples.removeFirst()
    }
}
