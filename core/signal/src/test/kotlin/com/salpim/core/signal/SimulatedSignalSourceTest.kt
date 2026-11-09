package com.salpim.core.signal

import com.salpim.core.model.VehicleSignal
import com.salpim.core.signal.sim.Scenarios
import com.salpim.core.signal.sim.SimulatedSignalSource
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatedSignalSourceTest {
    @Test
    fun playEmitsScenarioFramesWithClockTimestamps() = runTest {
        val src = SimulatedSignalSource(backgroundScope, clock = { testScheduler.currentTime }, tickMs = 200)
        val seen = mutableListOf<VehicleSignal>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { src.signals.toList(seen) }
        src.start()
        src.play(Scenarios.ENGINE_OIL_WARNING)
        advanceTimeBy(20_000)
        assertEquals(SignalSourceKind.SIMULATED, src.kind)
        assertTrue(seen.any { (it.speedKmh ?: 0f) > 55f })
        assertTrue(seen.last().activeWarnings.isNotEmpty())
        src.stop()
    }

    @Test
    fun setOverridesCurrentState() = runTest {
        val src = SimulatedSignalSource(backgroundScope, clock = { testScheduler.currentTime })
        src.set { copy(speedKmh = 42f) }
        assertEquals(42f, src.signals.value.speedKmh!!, 0.001f)
        src.reset()
        assertEquals(0f, src.signals.value.speedKmh!!, 0.001f)
    }
}
