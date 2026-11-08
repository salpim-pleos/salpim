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
    fun tireWarningAppearsWhileDriving() {
        val frames = Scenarios.TIRE_WARNING.frames(200, Scenarios.parked())
        val first = frames.first { WarningCodes.TIRE_PRESSURE_LOW in it.activeWarnings }
        assertEquals(Gear.DRIVE, first.gear)
        assertTrue(first.speedKmh!! > 50f)
    }

    @Test
    fun parkingScenarioEndsParked() {
        val last = Scenarios.PARKING_AFTER_WARNING.frames(200, Scenarios.parked()).last()
        assertEquals(Gear.PARK, last.gear)
        assertEquals(0f, last.speedKmh!!, 0.001f)
        assertTrue(WarningCodes.TIRE_PRESSURE_LOW in last.activeWarnings)
    }
}
