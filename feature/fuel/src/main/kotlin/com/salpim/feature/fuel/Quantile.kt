package com.salpim.feature.fuel

/**
 * 경험적 분위수 (선형 보간, Hyndman & Fan type 7 — R/NumPy 기본값과 같다).
 * @param p 0.0..1.0
 */
fun List<Double>.quantile(p: Double): Double {
    require(isNotEmpty()) { "quantile of empty list" }
    require(p in 0.0..1.0) { "p must be in 0..1" }
    val sorted = sorted()
    val position = (sorted.size - 1) * p
    val lowerIndex = position.toInt()
    val upperIndex = minOf(lowerIndex + 1, sorted.lastIndex)
    val fraction = position - lowerIndex
    return sorted[lowerIndex] + (sorted[upperIndex] - sorted[lowerIndex]) * fraction
}
