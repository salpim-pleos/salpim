package com.salpim.feature.fuel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RefuelAdvisorTest {
    /** 연비 샘플 1..10 km/% → 25% 분위 3.25, 10% 분위 1.9 */
    private val samples = (1..10).map { it.toDouble() }
    private val advisor = RefuelAdvisor(config = RefuelConfig(reserveKm = 30.0, criticalKm = 40.0))

    @Test
    fun historyRangeUsesLowerQuantiles() {
        val range = RangeEstimator().estimate(fuelPercent = 100.0, kmPerPercentSamples = samples)
        assertEquals(EstimateBasis.HISTORY, range.basis)
        assertEquals(325.0, range.likelyKm, 1e-9)
        assertEquals(190.0, range.conservativeKm, 1e-9)
    }

    @Test
    fun fewSamplesFallBackToDefaultEfficiency() {
        val range = RangeEstimator(RangeConfig(minSamples = 5, defaultKmPerPercent = 6.0)).estimate(50.0, listOf(5.0))
        assertEquals(EstimateBasis.DEFAULT, range.basis)
        assertTrue(range.conservativeKm < range.likelyKm)
    }

    @Test
    fun unknownFuelGivesNoAdvice() {
        assertEquals(RefuelAdvice.Unknown, advisor.advise(null, samples, destinationRemainingKm = 50.0))
    }

    @Test
    fun enoughWhenConservativeRangeCoversTrip() {
        // 90% 190km ≥ 필요 150+30
        val advice = advisor.advise(100.0, samples, destinationRemainingKm = 150.0)
        assertTrue(advice is RefuelAdvice.Enough)
        assertFalse(advice.showsStations)
    }

    @Test
    fun onTheWayWhenOnlyLikelyRangeCoversTrip() {
        // 90% 190km < 필요 250 ≤ 75% 325km
        val advice = advisor.advise(100.0, samples, destinationRemainingKm = 220.0)
        assertTrue(advice is RefuelAdvice.RefuelOnTheWay)
        assertTrue(advice.showsStations)
    }

    @Test
    fun nowWhenEvenLikelyRangeFallsShort() {
        // 75% 325km < 필요 330
        val advice = advisor.advise(100.0, samples, destinationRemainingKm = 300.0)
        assertTrue(advice is RefuelAdvice.RefuelNow)
    }

    @Test
    fun criticalRangeMeansNowEvenWithoutDestination() {
        // 10% 잔량 → 90% 19km < 위험 40km
        val advice = advisor.advise(10.0, samples, destinationRemainingKm = null)
        assertTrue(advice is RefuelAdvice.RefuelNow)
    }

    @Test
    fun boundaryConservativeEqualToNeededIsEnough() {
        // 90% 190km == 필요 160+30
        val advice = advisor.advise(100.0, samples, destinationRemainingKm = 160.0)
        assertTrue(advice is RefuelAdvice.Enough)
    }
}
