package com.salpim.feature.glance

import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.core.model.WarningCode
import com.salpim.core.model.WarningCodes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlanceModelTest {
    private fun exp(code: WarningCode, urgency: Urgency, title: String = code.value) =
        Explanation(code, title, title, "", emptyList(), listOf("a"), urgency, needsService = true, known = true)

    @Test
    fun neverMoreThanThreeItems() {
        assertTrue(GlanceModel.build(emptyList(), 100, maxItems = 10).size <= 3)
        assertEquals(2, GlanceModel.build(emptyList(), 100, maxItems = 2).size)
    }

    @Test
    fun mostUrgentVehicleWarningIsShown() {
        val items = GlanceModel.build(
            listOf(exp(WarningCodes.TIRE_PRESSURE_LOW, Urgency.SERVICE_SOON), exp(WarningCodes.BRAKE_SYSTEM, Urgency.IMMEDIATE_STOP, "브레이크")),
            90,
        )
        assertEquals("브레이크", items.first { it.id == "warning" }.value)
        assertEquals(GlanceTone.ALERT, items.first { it.id == "warning" }.tone)
    }

    @Test
    fun energyWarningsGoToEnergyItem() {
        val items = GlanceModel.build(listOf(exp(WarningCodes.LOW_FUEL, Urgency.INFO, "연료 부족")), 90)
        assertEquals("이상 없음", items.first { it.id == "warning" }.value)
        assertEquals("연료 부족", items.first { it.id == "energy" }.value)
    }
}
