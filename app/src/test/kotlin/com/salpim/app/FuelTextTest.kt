package com.salpim.app

import com.salpim.feature.fuel.EstimateBasis
import com.salpim.feature.fuel.RangeEstimate
import com.salpim.feature.fuel.RefuelAdvice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FuelTextTest {
    private fun range(conservativeKm: Double, likelyKm: Double) = RangeEstimate(
        likelyKm,
        conservativeKm,
        EstimateBasis.HISTORY,
        sampleCount = 20,
    )

    @Test
    fun likelyRangeSitsAtEightyPercentWhenNoDestination() {
        val bar = FuelText.bar(range(conservativeKm = 200.0, likelyKm = 400.0), neededKm = null)
        assertEquals(0.8f, bar.likelyFraction, DELTA)
        assertEquals(0.4f, bar.conservativeFraction, DELTA)
        assertNull(bar.needFraction)
    }

    @Test
    fun longerNeedSetsTheScaleSoRangeFallsShortVisibly() {
        val bar = FuelText.bar(range(conservativeKm = 100.0, likelyKm = 150.0), neededKm = 300.0)
        assertEquals(0.8f, bar.needFraction ?: 0f, DELTA)
        assertEquals(0.4f, bar.likelyFraction, DELTA)
    }

    @Test
    fun zeroRangeDoesNotDivideByZero() {
        val bar = FuelText.bar(range(conservativeKm = 0.0, likelyKm = 0.0), neededKm = null)
        assertEquals(0f, bar.likelyFraction, DELTA)
    }

    @Test
    fun rangeTextRoundsToTenKm() {
        val summary = FuelText.summary(RefuelAdvice.Enough(range(conservativeKm = 187.0, likelyKm = 334.0)))
        assertEquals("190~330km", summary?.rangeText)
    }

    @Test
    fun unknownFuelShowsNothing() {
        assertNull(FuelText.summary(RefuelAdvice.Unknown))
    }

    private companion object {
        const val DELTA = 0.001f
    }
}
