package com.salpim.feature.fuel

enum class EstimateBasis {
    /** 이 차의 주행 기록 분포 */
    HISTORY,

    /** 기록이 부족해 기본 연비로 계산 (정확도 낮음) */
    DEFAULT,
}

/**
 * 남은 연료로 갈 수 있는 거리.
 * @property likelyKm 지금까지 주행의 75%에서 이 거리 이상 갔다 (연비 하위 25% 분위로 계산)
 * @property conservativeKm 지금까지 주행의 90%에서 이 거리 이상 갔다 (연비 하위 10% 분위로 계산)
 */
data class RangeEstimate(
    val likelyKm: Double,
    val conservativeKm: Double,
    val basis: EstimateBasis,
    val sampleCount: Int,
)

data class RangeConfig(
    /** 이보다 샘플이 적으면 기본 연비를 쓴다 */
    val minSamples: Int = 5,
    /** 기록이 없을 때의 연료 1%당 km [설계] — 차종 정보(CarInfo)가 확인되면 차종별 값으로 */
    val defaultKmPerPercent: Double = 6.0,
    /** 기본 연비를 쓸 때 75%/90% 범위를 흉내 내는 할인율 [설계] */
    val defaultLikelyFactor: Double = 0.85,
    val defaultConservativeFactor: Double = 0.75,
)

class RangeEstimator(private val config: RangeConfig = RangeConfig()) {
    fun estimate(fuelPercent: Double, kmPerPercentSamples: List<Double>): RangeEstimate {
        if (kmPerPercentSamples.size < config.minSamples) return defaultEstimate(fuelPercent, kmPerPercentSamples.size)
        return RangeEstimate(
            likelyKm = fuelPercent * kmPerPercentSamples.quantile(1 - Coverage.LIKELY),
            conservativeKm = fuelPercent * kmPerPercentSamples.quantile(1 - Coverage.CONSERVATIVE),
            basis = EstimateBasis.HISTORY,
            sampleCount = kmPerPercentSamples.size,
        )
    }

    private fun defaultEstimate(fuelPercent: Double, sampleCount: Int): RangeEstimate {
        val nominalKm = fuelPercent * config.defaultKmPerPercent
        return RangeEstimate(
            likelyKm = nominalKm * config.defaultLikelyFactor,
            conservativeKm = nominalKm * config.defaultConservativeFactor,
            basis = EstimateBasis.DEFAULT,
            sampleCount = sampleCount,
        )
    }
}

/** 남은 거리 예측이 지금까지 주행을 얼마나 덮는지 */
object Coverage {
    const val LIKELY = 0.75
    const val CONSERVATIVE = 0.90
}
