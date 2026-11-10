package com.salpim.app

import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.core.model.WarningCode
import com.salpim.feature.fuel.EstimateBasis
import com.salpim.feature.fuel.RangeEstimate
import com.salpim.feature.fuel.RefuelAdvice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoPlanTest {
    private fun warning(code: String, urgency: Urgency) = Explanation(
        code = WarningCode(code),
        title = code,
        oneLine = code,
        detail = "",
        causes = emptyList(),
        actions = emptyList(),
        urgency = urgency,
        needsService = true,
        known = true,
    )

    private val range =
        RangeEstimate(likelyKm = 130.0, conservativeKm = 90.0, basis = EstimateBasis.HISTORY, sampleCount = 10)
    private val refuelNow = RefuelAdvice.RefuelNow(range, neededKm = 250.0)
    private val onTheWay = RefuelAdvice.RefuelOnTheWay(range, neededKm = 120.0)
    private fun fuel(advice: RefuelAdvice) = FuelText.summary(advice)

    @Test
    fun immediateStopComesBeforeRefuelNowWhichComesBeforeService() {
        val todos = TodoPlan.todos(
            listOf(warning("OIL", Urgency.SERVICE_SOON), warning("POWER", Urgency.IMMEDIATE_STOP)),
            fuel(refuelNow),
            refuelNow,
        )
        assertEquals(
            listOf<TodoKey>(TodoKey.Warning(WarningCode("POWER")), TodoKey.Refuel, TodoKey.Warning(WarningCode("OIL"))),
            todos.map { it.key },
        )
    }

    @Test
    fun refuelOnTheWayWaitsBehindServiceWarnings() {
        val todos = TodoPlan.todos(listOf(warning("OIL", Urgency.SERVICE_SOON)), fuel(onTheWay), onTheWay)
        assertEquals(TodoKey.Warning(WarningCode("OIL")), todos.first().key)
    }

    @Test
    fun enoughFuelIsNotATodoAndListIsCapped() {
        val enough = RefuelAdvice.Enough(range)
        val warnings = (1..5).map { warning("W$it", Urgency.INFO) }
        val todos = TodoPlan.todos(warnings, fuel(enough), enough)
        assertTrue(todos.none { it is Todo.Refuel })
        assertEquals(TodoPlan.MAX_TODOS, todos.size)
    }

    @Test
    fun panelPrefersOpenListsThenSelectionThenMostUrgent() {
        val todos = TodoPlan.todos(listOf(warning("OIL", Urgency.SERVICE_SOON)), fuel(refuelNow), refuelNow)
        assertEquals(PanelMode.Places, TodoPlan.panel(todos, null, showingPlaces = true, showingReservation = true))
        assertEquals(
            PanelMode.Reservation,
            TodoPlan.panel(todos, null, showingPlaces = false, showingReservation = true),
        )
        val selected = TodoPlan.panel(
            todos,
            TodoKey.Warning(WarningCode("OIL")),
            showingPlaces = false,
            showingReservation = false,
        )
        assertEquals(TodoKey.Warning(WarningCode("OIL")), (selected as PanelMode.Doing).todo.key)
        val default = TodoPlan.panel(todos, null, showingPlaces = false, showingReservation = false)
        assertEquals(TodoKey.Refuel, (default as PanelMode.Doing).todo.key)
        assertEquals(
            PanelMode.Nothing,
            TodoPlan.panel(emptyList(), null, showingPlaces = false, showingReservation = false),
        )
    }
}
