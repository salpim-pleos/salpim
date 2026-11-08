package com.salpim.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UxPolicyTest {
    @Test
    fun drivingIsAlwaysRestrictedEvenWithoutSystemFlags() {
        val p = UxPolicy.resolve(DrivingState.DRIVING, UxRestrictionFlags.NONE)
        assertFalse(p.detailAllowed)
        assertFalse(p.textInputAllowed)
        assertFalse(p.scrollingAllowed)
        assertEquals(3, p.maxGlanceItems)
    }

    @Test
    fun systemRestrictionWinsWhileParked() {
        val p = UxPolicy.resolve(
            DrivingState.PARKED,
            UxRestrictionFlags(requiresDistractionOptimization = true, maxStringLength = 20),
        )
        assertTrue(p.restricted)
        assertEquals(20, p.maxStringLength)
    }

    @Test
    fun parkedWithoutRestrictionAllowsDetail() {
        val p = UxPolicy.resolve(DrivingState.PARKED, UxRestrictionFlags.NONE)
        assertTrue(p.detailAllowed)
        assertTrue(p.scrollingAllowed)
    }

    @Test
    fun ellipsizeRespectsMax() {
        assertEquals("abcd…", "abcdefgh".ellipsize(5))
        assertEquals("abc", "abc".ellipsize(5))
        assertEquals("abc", "abc".ellipsize(null))
    }
}
