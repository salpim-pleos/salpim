package com.salpim.core.signal.pleos

import ai.pleos.playground.vehicle.constant.control.OilLevelState
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import com.salpim.core.model.Gear
import com.salpim.core.model.WarningCodes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PleosMappingTest {
    @Test
    fun lowAndCriticalOilMapToTwoUrgencyLevels() {
        assertEquals(WarningCodes.ENGINE_OIL_LOW, PleosMapping.oilWarning(OilKind.ENGINE, OilLevelState.LOW))
        assertEquals(
            WarningCodes.BRAKE_FLUID_CRITICAL,
            PleosMapping.oilWarning(OilKind.BRAKE_FLUID, OilLevelState.CRITICALLY_LOW),
        )
        assertEquals(
            WarningCodes.TRANSMISSION_OIL_LOW,
            PleosMapping.oilWarning(OilKind.TRANSMISSION, OilLevelState.LOW),
        )
    }

    @Test
    fun normalHighErrorAndUnknownOilGiveNoWarning() {
        listOf(OilLevelState.NORMAL, OilLevelState.HIGH, OilLevelState.ERROR, null).forEach {
            assertNull(PleosMapping.oilWarning(OilKind.ENGINE, it))
        }
    }

    @Test
    fun flagsWarnOnlyWhenTrue() {
        assertEquals(WarningCodes.POWER_DOWN, PleosMapping.flagWarning(FlagKind.POWER_DOWN, true))
        assertNull(PleosMapping.flagWarning(FlagKind.WASHER_FLUID, false))
        assertNull(PleosMapping.flagWarning(FlagKind.AIRBAG_DEPLOYED, null))
    }

    @Test
    fun gearsMapIncludingManualSteps() {
        assertEquals(Gear.PARK, PleosMapping.gear(VehicleGear.GEAR_PARK))
        assertEquals(Gear.DRIVE, PleosMapping.gear(VehicleGear.GEAR_3))
        assertEquals(Gear.UNKNOWN, PleosMapping.gear(null))
    }

    @Test
    fun readingsBecomeSignalWithoutSpeed() {
        val signal = PleosReadings(
            gear = Gear.DRIVE,
            fuelPercent = 40.0,
            oil = mapOf(OilKind.ENGINE to WarningCodes.ENGINE_OIL_LOW, OilKind.BRAKE_FLUID to null),
        ).toSignal(timestampMs = 7)
        assertNull(signal.speedKmh)
        assertEquals(setOf(WarningCodes.ENGINE_OIL_LOW), signal.activeWarnings)
        assertEquals(7L, signal.timestampMs)
    }
}
