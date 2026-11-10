package com.salpim.core.signal

import com.salpim.core.model.Gear
import com.salpim.core.model.WarningCodes
import com.salpim.core.signal.sim.Scenarios
import com.salpim.core.signal.sim.scenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenarioTest {
    @Test
    fun framesAreDeterministicAndEvenlySpaced() {
        val a = Scenarios.HARD_BRAKE_SEQUENCE.frames(200, Scenarios.parked(1_000))
        val b = Scenarios.HARD_BRAKE_SEQUENCE.frames(200, Scenarios.parked(1_000))
        assertEquals(a, b)
        a.zipWithNext().forEach { (p, n) -> assertEquals(200, n.timestampMs - p.timestampMs) }
    }

    @Test
    fun rampReachesTarget() {
        val s = scenario("t", "t") { ramp(60f, 1_000) }
        val frames = s.frames(100, Scenarios.parked())
        assertEquals(10, frames.size)
        assertEquals(60f, frames.last().speedKmh!!, 0.001f)
    }

    @Test
    fun engineOilWarningAppearsWhileDriving() {
        val frames = Scenarios.ENGINE_OIL_WARNING.frames(200, Scenarios.parked())
        val first = frames.first { WarningCodes.ENGINE_OIL_LOW in it.activeWarnings }
        assertEquals(Gear.DRIVE, first.gear)
        assertTrue(first.speedKmh!! > 50f)
    }

    @Test
    fun drivingAdvancesOdometerAndBurnsFuel() {
        val start = Scenarios.parked()
        val last = Scenarios.ENGINE_OIL_WARNING.frames(200, start).last()
        assertTrue(last.odometerKm!! > start.odometerKm!!)
        assertTrue(last.fuelPercent!! < start.fuelPercent!!)
    }

    @Test
    fun scenarioSetOverridesFuelAndRoute() {
        val frames = Scenarios.LOW_FUEL_TRIP.frames(200, Scenarios.parked())
        assertTrue(frames.first().fuelPercent!! <= 18.0)
        assertTrue(frames.last().routeRemainingKm!! < 220.0)
    }

    @Test
    fun cityHallTripHeadsToCityHallThenParksWithWarning() {
        val frames = Scenarios.CITY_HALL_TRIP.frames(200, Scenarios.parked())
        val driving = frames.first { it.gear == Gear.DRIVE }
        assertEquals(Scenarios.CITY_HALL, driving.destinationName)
        assertTrue(frames.any { it.destinationName != null && WarningCodes.ENGINE_OIL_LOW in it.activeWarnings })
        val last = frames.last()
        assertEquals(Gear.PARK, last.gear)
        assertEquals(null, last.destinationName)
        assertEquals(null, last.routeRemainingKm)
    }

    @Test
    fun airportTripFindsRouteAfterDepartingThenWarnsAndParks() {
        val frames = Scenarios.AIRPORT_TRIP.frames(200, Scenarios.parked())
        val firstWithRoute = frames.indexOfFirst { it.routeRemainingKm != null }
        val firstDriving = frames.indexOfFirst { it.gear == Gear.DRIVE }
        assertTrue(firstDriving < firstWithRoute)
        assertEquals(Scenarios.AIRPORT, frames[firstWithRoute].destinationName)
        val firstWarning = frames.indexOfFirst { WarningCodes.ENGINE_OIL_LOW in it.activeWarnings }
        assertTrue(firstWithRoute < firstWarning)
        assertEquals(Gear.PARK, frames.last().gear)
        assertEquals(Scenarios.AIRPORT_TRIP, Scenarios.forDestination("인천공항"))
        assertEquals(null, Scenarios.forDestination("부산"))
    }

    @Test
    fun parkingScenarioEndsParked() {
        val last = Scenarios.PARKING_AFTER_WARNING.frames(200, Scenarios.parked()).last()
        assertEquals(Gear.PARK, last.gear)
        assertEquals(0f, last.speedKmh!!, 0.001f)
        assertTrue(WarningCodes.ENGINE_OIL_LOW in last.activeWarnings)
    }
}
