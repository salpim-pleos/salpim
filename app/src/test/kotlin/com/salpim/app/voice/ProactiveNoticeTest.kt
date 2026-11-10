package com.salpim.app.voice

import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.core.model.WarningCode
import com.salpim.feature.fuel.EstimateBasis
import com.salpim.feature.fuel.RangeEstimate
import com.salpim.feature.fuel.RefuelAdvice
import com.salpim.feature.service.PlaceCategory
import com.salpim.feature.voice.DialogContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProactiveNoticeTest {
    private fun warning(code: String, urgency: Urgency, oneLine: String = "엔진오일이 부족해요. 정차 후 점검하세요") = Explanation(
        code = WarningCode(code),
        title = code,
        oneLine = oneLine,
        detail = "",
        causes = emptyList(),
        actions = emptyList(),
        urgency = urgency,
        needsService = true,
        known = true,
    )

    private val range =
        RangeEstimate(likelyKm = 106.0, conservativeKm = 64.0, basis = EstimateBasis.HISTORY, sampleCount = 10)
    private val enough = RefuelAdvice.Enough(range)
    private val onTheWay = RefuelAdvice.RefuelOnTheWay(range, neededKm = 82.0)

    @Test
    fun newServiceWarningIsSpokenOnceAsOneLine() {
        val oil = warning("OIL", Urgency.SERVICE_SOON)
        val reply = ProactiveNotice.between(NoticeSnapshot(emptyList(), enough), NoticeSnapshot(listOf(oil), enough))
        assertEquals("엔진오일이 부족해요. 정차 후 점검하세요.", reply?.text)
        assertNull(ProactiveNotice.between(NoticeSnapshot(listOf(oil), enough), NoticeSnapshot(listOf(oil), enough)))
    }

    @Test
    fun infoWarningsStayQuiet() {
        val info = warning("WASHER", Urgency.INFO)
        assertNull(ProactiveNotice.between(NoticeSnapshot(emptyList(), enough), NoticeSnapshot(listOf(info), enough)))
    }

    @Test
    fun enteringRefuelOnTheWayOffersToFindStations() {
        val reply = ProactiveNotice.between(NoticeSnapshot(emptyList(), enough), NoticeSnapshot(emptyList(), onTheWay))
        assertEquals(VoiceEffect.FindPlaces(PlaceCategory.GAS_STATION), reply?.pending)
        assertEquals(DialogContext.AwaitingConfirmation, reply?.nextContext)
        assertNull(
            ProactiveNotice.between(NoticeSnapshot(emptyList(), onTheWay), NoticeSnapshot(emptyList(), onTheWay)),
        )
    }

    @Test
    fun warningWinsOverFuelWhenBothAppear() {
        val oil = warning("OIL", Urgency.SERVICE_SOON)
        val reply = ProactiveNotice.between(NoticeSnapshot(emptyList(), enough), NoticeSnapshot(listOf(oil), onTheWay))
        assertNull(reply?.pending)
    }
}
