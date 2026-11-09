package com.salpim.feature.fuel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FuelMonitorTest {
    @Test
    fun missingFuelGivesUnknown() {
        assertEquals(
            RefuelAdvice.Unknown,
            FuelMonitor().onReading(odometerKm = 100.0, fuelPercent = null, routeRemainingKm = 10.0),
        )
    }

    @Test
    fun seededHistoryIsUsedForRange() {
        val monitor = FuelMonitor(EfficiencyTracker(initialKmPerPercent = List(10) { 6.0 }))
        val advice = monitor.onReading(odometerKm = 0.0, fuelPercent = 50.0, routeRemainingKm = 100.0)
        assertTrue(advice is RefuelAdvice.Enough)
        assertEquals(EstimateBasis.HISTORY, (advice as RefuelAdvice.Enough).range.basis)
        assertEquals(300.0, advice.range.conservativeKm, 1e-9)
    }

    @Test
    fun lowFuelLongTripMeansRefuelNow() {
        val monitor = FuelMonitor(EfficiencyTracker(initialKmPerPercent = List(10) { 6.0 }))
        val advice = monitor.onReading(odometerKm = 0.0, fuelPercent = 18.0, routeRemainingKm = 220.0)
        assertTrue(advice is RefuelAdvice.RefuelNow)
    }
}
