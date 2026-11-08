package com.salpim.feature.coach

import com.salpim.core.model.DrivingEventType
import com.salpim.core.model.VehicleSignal
import com.salpim.core.signal.sim.Scenarios
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitDetectorTest {
    private fun detect(frames: List<VehicleSignal>, config: HabitConfig = HabitConfig()) =
        HabitDetector(config).let { d -> frames.mapNotNull(d::process) }

    @Test
    fun hardBrakeScenarioIsDetectedReproducibly() {
        val frames = Scenarios.HARD_BRAKE_SEQUENCE.frames(200, Scenarios.parked())
        val a = detect(frames)
        val b = detect(frames)
        assertEquals(a, b)
        assertEquals(1, a.count { it.type == DrivingEventType.HARD_BRAKE })
        assertEquals(1, a.count { it.type == DrivingEventType.HARD_ACCEL })
    }

    @Test
    fun gentleScenariosProduceNoEvents() {
        assertTrue(detect(Scenarios.TIRE_WARNING.frames(200, Scenarios.parked())).isEmpty())
        assertTrue(detect(Scenarios.PARKING_AFTER_WARNING.frames(200, Scenarios.parked())).isEmpty())
    }

    @Test
    fun sharpTurnNeedsSpeed() {
        val slow = listOf(
            VehicleSignal(0, speedKmh = 10f, steeringAngleDeg = 0f),
            VehicleSignal(200, speedKmh = 10f, steeringAngleDeg = 180f),
        )
        assertTrue(detect(slow).isEmpty())
        val fast = slow.map { it.copy(speedKmh = 60f) }
        assertEquals(DrivingEventType.SHARP_TURN, detect(fast).single().type)
    }

    @Test
    fun reportScoreAndTips() {
        val events = detect(Scenarios.HARD_BRAKE_SEQUENCE.frames(200, Scenarios.parked()))
        val r = CoachReport.from(events)
        assertEquals(83, r.score)
        assertEquals(2, r.tips.size)
        assertEquals(100, CoachReport.from(emptyList()).score)
    }
}
