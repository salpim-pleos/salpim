package com.salpim.app

import com.salpim.core.model.CarZone
import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.core.model.WarningCodes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CarPartsTest {
    private fun warning(zone: CarZone, urgency: Urgency, title: String) = Explanation(
        code = WarningCodes.ENGINE_OIL_LOW,
        title = title,
        oneLine = title,
        detail = "",
        causes = emptyList(),
        actions = emptyList(),
        urgency = urgency,
        needsService = true,
        known = true,
        zone = zone,
        plainMeaning = "쉬운 설명",
    )

    @Test
    fun allSixPartsAreListedAndNormalWithoutWarnings() {
        val parts = CarParts.statuses(emptyList())
        assertEquals(CarZone.entries.toSet(), parts.map { it.zone }.toSet())
        parts.forEach { assertNull(it.issue) }
    }

    @Test
    fun partWithIssueShowsMostUrgentWarning() {
        val parts = CarParts.statuses(
            listOf(
                warning(CarZone.ENGINE, Urgency.SERVICE_SOON, "엔진오일 부족"),
                warning(CarZone.ENGINE, Urgency.IMMEDIATE_STOP, "엔진 이상 신호"),
            ),
        )
        val engine = parts.single { it.zone == CarZone.ENGINE }
        assertEquals("엔진 이상 신호", engine.issue?.title)
        assertEquals("엔진오일", engine.name)
    }

    @Test
    fun partOrderStaysFixedSoTilesDoNotMove() {
        val normal = CarParts.statuses(emptyList()).map { it.zone }
        val withSeatBeltIssue = CarParts.statuses(
            listOf(warning(CarZone.SEAT_BELT, Urgency.INFO, "안전벨트")),
        ).map { it.zone }
        assertEquals(normal, withSeatBeltIssue)
    }
}
