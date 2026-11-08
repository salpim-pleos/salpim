package com.salpim.app

import com.salpim.core.data.InMemoryHistoryRepository
import com.salpim.core.model.DrivingEventType
import com.salpim.core.model.DrivingState
import com.salpim.core.model.UxRestrictionFlags
import com.salpim.core.model.WarningCodes
import com.salpim.core.signal.sim.Scenarios
import com.salpim.core.signal.sim.SimulatedSignalSource
import com.salpim.core.state.DrivingStateConfig
import com.salpim.core.ux.StaticUxRestrictionsSource
import com.salpim.feature.coach.HabitDetector
import com.salpim.feature.warning.WarningExplainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** 앱 파이프라인 통합 테스트 (JVM, 가상 시간). 화면 단 e2e는 androidTest 참고. */
class SalpimEngineTest {
    private val explainer = WarningExplainer.fromJson(
        File("../feature/warning/src/main/assets/${WarningExplainer.ASSET_NAME}").readText(Charsets.UTF_8),
    )

    private class Rig(scope: CoroutineScope, test: TestScope, explainer: WarningExplainer) {
        val sim = SimulatedSignalSource(scope, clock = { test.testScheduler.currentTime }, tickMs = 200)
        val ux = StaticUxRestrictionsSource()
        val history = InMemoryHistoryRepository()
        val engine = SalpimEngine(scope, sim, ux, DrivingStateConfig(), explainer, HabitDetector(), history)
    }

    @Test
    fun parkingAfterWarningScenarioEndsParkedWithWarningExplained() = runTest {
        val rig = Rig(backgroundScope, this, explainer)
        rig.engine.start()
        runCurrent()
        assertEquals(DrivingState.PARKED, rig.engine.state.value.drivingState)

        rig.sim.play(Scenarios.PARKING_AFTER_WARNING)
        advanceTimeBy(5_000)
        assertEquals(DrivingState.DRIVING, rig.engine.state.value.drivingState)
        assertEquals(WarningCodes.TIRE_PRESSURE_LOW, rig.engine.state.value.activeWarnings.single().code)

        advanceTimeBy(30_000)
        val s = rig.engine.state.value
        assertEquals(DrivingState.PARKED, s.drivingState)
        assertTrue(s.activeWarnings.single().known)
        assertEquals(listOf(WarningCodes.TIRE_PRESSURE_LOW), s.seenWarnings)
        rig.engine.stop()
    }

    @Test
    fun hardBrakeScenarioRecordsEvents() = runTest {
        val rig = Rig(backgroundScope, this, explainer)
        rig.engine.start()
        rig.sim.play(Scenarios.HARD_BRAKE_SEQUENCE)
        advanceTimeBy(40_000)
        val types = rig.engine.state.value.events.map { it.type }
        assertTrue(DrivingEventType.HARD_BRAKE in types)
        assertTrue(DrivingEventType.HARD_ACCEL in types)

        rig.engine.clearHistory()
        assertTrue(rig.engine.state.value.events.isEmpty())
        assertTrue(rig.history.events.value.isEmpty())
        rig.engine.stop()
    }

    @Test
    fun systemRestrictionIsReflectedWhileParked() = runTest {
        val rig = Rig(backgroundScope, this, explainer)
        rig.engine.start()
        rig.ux.set(UxRestrictionFlags(requiresDistractionOptimization = true))
        runCurrent()
        assertTrue(rig.engine.state.value.ux.requiresDistractionOptimization)
        rig.engine.stop()
    }
}
