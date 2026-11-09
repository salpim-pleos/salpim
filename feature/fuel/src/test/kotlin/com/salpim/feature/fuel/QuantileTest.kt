package com.salpim.feature.fuel

import org.junit.Assert.assertEquals
import org.junit.Test

class QuantileTest {
    private val values = listOf(5.0, 1.0, 4.0, 2.0, 3.0)

    @Test
    fun endsAreMinAndMax() {
        assertEquals(1.0, values.quantile(0.0), 1e-9)
        assertEquals(5.0, values.quantile(1.0), 1e-9)
    }

    @Test
    fun interpolatesLikeType7() {
        // NumPy: np.quantile([1,2,3,4,5], 0.25) == 2.0, 0.1 == 1.4
        assertEquals(2.0, values.quantile(0.25), 1e-9)
        assertEquals(1.4, values.quantile(0.10), 1e-9)
    }

    @Test
    fun singleValueIsItself() {
        assertEquals(7.0, listOf(7.0).quantile(0.9), 1e-9)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyListIsRejected() {
        emptyList<Double>().quantile(0.5)
    }
}
