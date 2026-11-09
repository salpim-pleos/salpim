package com.salpim.feature.fuel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EfficiencyTrackerTest {
    @Test
    fun segmentClosesAfterMinimumFuelUse() {
        val tracker = EfficiencyTracker(EfficiencyConfig(minSegmentPercent = 2.0))
        tracker.record(FuelReading(odometerKm = 1000.0, fuelPercent = 80.0))
        tracker.record(FuelReading(odometerKm = 1006.0, fuelPercent = 79.0))
        assertTrue("1% 사용은 아직 샘플 아님", tracker.kmPerPercentSamples.isEmpty())
        tracker.record(FuelReading(odometerKm = 1012.0, fuelPercent = 78.0))
        assertEquals(listOf(6.0), tracker.kmPerPercentSamples)
    }

    @Test
    fun refuelRestartsSegmentWithoutSample() {
        val tracker = EfficiencyTracker()
        tracker.record(FuelReading(1000.0, 20.0))
        tracker.record(FuelReading(1001.0, 90.0))
        tracker.record(FuelReading(1013.0, 88.0))
        assertEquals(listOf(6.0), tracker.kmPerPercentSamples)
    }

    @Test
    fun keepsOnlyMostRecentSamples() {
        val tracker = EfficiencyTracker(EfficiencyConfig(minSegmentPercent = 1.0, maxSamples = 3))
        var odometerKm = 0.0
        var fuelPercent = 100.0
        tracker.record(FuelReading(odometerKm, fuelPercent))
        repeat(5) { i ->
            odometerKm += (i + 1).toDouble()
            fuelPercent -= 1.0
            tracker.record(FuelReading(odometerKm, fuelPercent))
        }
        assertEquals(listOf(3.0, 4.0, 5.0), tracker.kmPerPercentSamples)
    }
}
