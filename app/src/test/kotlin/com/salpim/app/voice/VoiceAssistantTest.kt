package com.salpim.app.voice

import com.salpim.core.model.CarZone
import com.salpim.core.model.DrivingEventType
import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.core.model.WarningCodes
import com.salpim.feature.fuel.EstimateBasis
import com.salpim.feature.fuel.RangeEstimate
import com.salpim.feature.fuel.RefuelAdvice
import com.salpim.feature.service.NearbyPlace
import com.salpim.feature.service.PlaceCategory
import com.salpim.feature.voice.ClarificationReason
import com.salpim.feature.voice.DialogContext
import com.salpim.feature.voice.VoiceIntent
import com.salpim.feature.voice.VoiceResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceAssistantTest {
    private val assistant = VoiceAssistant()
    private val oilLow = Explanation(
        code = WarningCodes.ENGINE_OIL_LOW,
        title = "엔진오일 부족",
        oneLine = "엔진오일이 부족해요. 정차 후 점검하세요",
        detail = "엔진오일 양이 기준보다 낮아요.",
        causes = emptyList(),
        actions = listOf("점검"),
        urgency = Urgency.SERVICE_SOON,
        needsService = true,
        known = true,
        zone = CarZone.ENGINE,
    )
    private val places = listOf(
        NearbyPlace("a", PlaceCategory.BLUE_HANDS, "블루핸즈 예시 A지점", "", null, 0.0, 0.0, 1.2),
        NearbyPlace("b", PlaceCategory.BLUE_HANDS, "블루핸즈 예시 B지점", "", null, 0.0, 0.0, 2.8),
    )
    private val range =
        RangeEstimate(likelyKm = 325.0, conservativeKm = 190.0, basis = EstimateBasis.HISTORY, sampleCount = 10)

    private fun facts(
        warning: Explanation? = null,
        fuel: RefuelAdvice = RefuelAdvice.Enough(range),
        shown: List<NearbyPlace> = emptyList(),
    ) =
        VoiceFacts(
            warning,
            fuel,
            score = 83,
            eventCounts = mapOf(DrivingEventType.HARD_BRAKE to 1),
            shownPlaces = shown,
        )

    private fun say(intent: VoiceIntent, facts: VoiceFacts = facts(), pending: VoiceEffect? = null) =
        assistant.reply(VoiceResolution.Accepted(intent), facts, pending)

    @Test
    fun statusWithoutWarningMentionsRange() {
        val reply = say(VoiceIntent.CheckStatus)
        assertTrue(reply.text.startsWith("모두 괜찮아요"))
        assertTrue(reply.text.contains("190~320km") || reply.text.contains("190~330km"))
    }

    @Test
    fun statusWithWarningReadsOneLine() {
        assertTrue(say(VoiceIntent.CheckStatus, facts(oilLow)).text.contains("엔진오일이 부족해요"))
    }

    @Test
    fun keepDrivingFollowsUrgency() {
        assertEquals("네, 지금은 괜찮아요.", say(VoiceIntent.CanKeepDriving).text)
        assertTrue(say(VoiceIntent.CanKeepDriving, facts(oilLow)).text.contains("점검"))
        assertTrue(
            say(VoiceIntent.CanKeepDriving, facts(oilLow.copy(urgency = Urgency.IMMEDIATE_STOP))).text.contains("세우는"),
        )
    }

    @Test
    fun findServiceCenterSearchesBlueHands() {
        assertEquals(VoiceEffect.FindPlaces(PlaceCategory.BLUE_HANDS), say(VoiceIntent.FindServiceCenter).effect)
    }

    @Test
    fun describedPlacesAskForOrdinalAndOfferNearest() {
        val reply = assistant.describePlaces(PlaceCategory.BLUE_HANDS, places)
        assertTrue(reply.text.contains("2곳") && reply.text.contains("1.2km"))
        assertEquals(DialogContext.ChoosingOption(2), reply.nextContext)
        assertEquals(VoiceEffect.RouteTo(places[0]), reply.pending)
    }

    @Test
    fun noPlacesFoundSaysSo() {
        assertTrue(assistant.describePlaces(PlaceCategory.GAS_STATION, emptyList()).text.contains("찾지 못했어요"))
    }

    @Test
    fun selectOptionRoutesToThatPlace() {
        assertEquals(VoiceEffect.RouteTo(places[1]), say(VoiceIntent.SelectOption(2), facts(shown = places)).effect)
    }

    @Test
    fun routeReplyAttachesParticleWithoutBrackets() {
        val reply = say(VoiceIntent.SelectOption(1), facts(shown = places))
        assertEquals("블루핸즈 예시 A지점으로 안내할게요.", reply.text)
    }

    @Test
    fun confirmRunsPendingRoute() {
        assertEquals(
            VoiceEffect.RouteTo(places[0]),
            say(VoiceIntent.Confirm, pending = VoiceEffect.RouteTo(places[0])).effect,
        )
    }

    @Test
    fun nextOptionMovesPendingToNextPlace() {
        val reply = say(VoiceIntent.NextOption, facts(shown = places), pending = VoiceEffect.RouteTo(places[0]))
        assertEquals(VoiceEffect.RouteTo(places[1]), reply.pending)
        assertNull(reply.effect)
    }

    @Test
    fun clearHistoryAsksBeforeDeleting() {
        val ask = say(VoiceIntent.ClearHistory)
        assertNull(ask.effect)
        assertEquals(DialogContext.AwaitingConfirmation, ask.nextContext)
        assertEquals(VoiceEffect.ClearHistory, say(VoiceIntent.Confirm, pending = ask.pending).effect)
    }

    @Test
    fun refuelNowIsSpokenClearly() {
        val reply = say(VoiceIntent.AskRange, facts(fuel = RefuelAdvice.RefuelNow(range, neededKm = 400.0)))
        assertTrue(reply.text.contains("지금 주유"))
    }

    @Test
    fun reportListsEvents() {
        assertTrue(say(VoiceIntent.AskReport).text.contains("급제동 1번"))
    }

    @Test
    fun clarificationExplainsWhatToSay() {
        val reply = assistant.reply(
            VoiceResolution.NeedsClarification(ClarificationReason.NOT_UNDERSTOOD, "음"),
            facts(),
            null,
        )
        assertTrue(reply.text.contains("차 상태 어때"))
    }
}
