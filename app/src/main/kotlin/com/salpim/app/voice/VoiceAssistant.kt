package com.salpim.app.voice

import com.salpim.app.FuelText
import com.salpim.core.model.DrivingEventType
import com.salpim.core.model.Explanation
import com.salpim.core.model.KoreanParticles
import com.salpim.core.model.Urgency
import com.salpim.feature.coach.label
import com.salpim.feature.fuel.RefuelAdvice
import com.salpim.feature.service.NearbyPlace
import com.salpim.feature.service.PlaceCategory
import com.salpim.feature.voice.ClarificationReason
import com.salpim.feature.voice.DialogContext
import com.salpim.feature.voice.VoiceIntent
import com.salpim.feature.voice.VoiceResolution

/** 대답에 필요한 지금 상태 */
data class VoiceFacts(
    val topWarning: Explanation?,
    val fuel: RefuelAdvice,
    val score: Int,
    val eventCounts: Map<DrivingEventType, Int>,
    /** 화면에 번호 붙어 보이는 장소 목록 */
    val shownPlaces: List<NearbyPlace>,
)

/** 대답과 함께 앱이 할 일 */
sealed interface VoiceEffect {
    data class FindPlaces(val category: PlaceCategory) : VoiceEffect
    data class RouteTo(val place: NearbyPlace) : VoiceEffect
    data object ShowReservation : VoiceEffect

    /** 내비 목적지 설정 (시뮬레이션에서는 그 목적지 시연 시나리오) */
    data class StartRoute(val destination: String) : VoiceEffect
    data object ClearHistory : VoiceEffect
}

/**
 * @property effect 지금 바로 할 일
 * @property pending "응"이라고 하면 할 일 (앱이 물어본 것)
 */
data class VoiceReply(
    val text: String,
    val effect: VoiceEffect? = null,
    val pending: VoiceEffect? = null,
    val nextContext: DialogContext = DialogContext.Idle,
)

/** 음성 명령 → 대답 문장 + 할 일. 순수 함수 (화면·엔진과 무관하게 테스트한다) */
class VoiceAssistant {
    fun reply(resolution: VoiceResolution, facts: VoiceFacts, pending: VoiceEffect?): VoiceReply = when (resolution) {
        is VoiceResolution.NeedsClarification -> clarify(resolution.reason)
        is VoiceResolution.Accepted -> answer(resolution.intent, facts, pending)
    }

    /**
     * 장소를 찾은 뒤 목록을 읽어 주고 번호로 고르게 한다.
     * 주유소 가격을 알면 가장 싼 곳을 먼저 권한다 ("응"이면 그곳으로). [onRoute]면 "가는 길에"로 말한다.
     */
    fun describePlaces(category: PlaceCategory, places: List<NearbyPlace>, onRoute: Boolean = false): VoiceReply {
        val first = places.firstOrNull() ?: return VoiceReply("근처에서 ${KoreanParticles.withObject(category.spoken())} 찾지 못했어요.")
        val cheapest = places.filter { it.priceWon != null }.minByOrNull { it.priceWon ?: Int.MAX_VALUE }
        if (category == PlaceCategory.GAS_STATION && cheapest?.priceWon != null) {
            val where = if (onRoute) "가는 길에 있는 주유소 중" else "가까운 주유소 중"
            return VoiceReply(
                text = "$where ${KoreanParticles.withSubject(
                    cheapest.name,
                )} 리터당 ${"%,d".format(cheapest.priceWon)}원으로 가장 싸요. 안내할까요?",
                pending = VoiceEffect.RouteTo(cheapest),
                nextContext = DialogContext.ChoosingOption(places.size),
            )
        }
        val distance = first.distanceKm?.let { "%.1fkm로 ".format(it) }.orEmpty()
        return VoiceReply(
            text = "${places.size}곳을 찾았어요. 첫 번째가 ${distance}가장 가까워요. 몇 번째로 갈까요?",
            pending = VoiceEffect.RouteTo(first),
            nextContext = DialogContext.ChoosingOption(places.size),
        )
    }

    private fun answer(intent: VoiceIntent, facts: VoiceFacts, pending: VoiceEffect?): VoiceReply = when (intent) {
        VoiceIntent.CheckStatus -> VoiceReply(statusSentence(facts))
        VoiceIntent.CanKeepDriving -> VoiceReply(keepDrivingSentence(facts.topWarning))
        VoiceIntent.ExplainIssue -> VoiceReply(facts.topWarning?.detail ?: "지금은 살펴볼 문제가 없어요.")
        VoiceIntent.FindServiceCenter -> VoiceReply(
            "가까운 블루핸즈를 찾을게요.",
            effect = VoiceEffect.FindPlaces(PlaceCategory.BLUE_HANDS),
        )
        VoiceIntent.FindGasStation -> VoiceReply(
            "가까운 주유소를 찾을게요.",
            effect = VoiceEffect.FindPlaces(PlaceCategory.GAS_STATION),
        )
        VoiceIntent.AskRange -> VoiceReply(rangeSentence(facts.fuel))
        is VoiceIntent.SelectOption -> routeTo(facts.shownPlaces[intent.ordinal - 1])
        VoiceIntent.NextOption -> nextOption(facts.shownPlaces, pending)
        is VoiceIntent.GoTo -> VoiceReply(
            "${KoreanParticles.withDirection(intent.destination)} 안내할게요.",
            effect = VoiceEffect.StartRoute(intent.destination),
        )
        VoiceIntent.OpenReservation -> VoiceReply(
            "휴대폰 카메라로 화면의 QR을 찍으면 예약 페이지가 열려요.",
            effect = VoiceEffect.ShowReservation,
        )
        VoiceIntent.AskReport -> VoiceReply(reportSentence(facts))
        VoiceIntent.ClearHistory -> VoiceReply(
            "운전 기록을 모두 지울까요?",
            pending = VoiceEffect.ClearHistory,
            nextContext = DialogContext.AwaitingConfirmation,
        )
        VoiceIntent.Confirm -> confirm(pending)
        VoiceIntent.Cancel -> VoiceReply("알겠어요.")
        is VoiceIntent.Unknown -> clarify(ClarificationReason.NOT_UNDERSTOOD)
    }

    private fun routeTo(place: NearbyPlace) = VoiceReply(
        "${KoreanParticles.withDirection(place.name)} 안내할게요.",
        effect = VoiceEffect.RouteTo(place),
    )

    private fun nextOption(places: List<NearbyPlace>, pending: VoiceEffect?): VoiceReply {
        val current = (pending as? VoiceEffect.RouteTo)?.place
        val next = places.getOrNull(places.indexOf(current) + 1) ?: return VoiceReply("더 가까운 곳은 없어요.")
        val distance = next.distanceKm?.let { ", %.1fkm".format(it) }.orEmpty()
        return VoiceReply(
            "다음은 ${next.name}$distance 예요. 여기로 갈까요?",
            pending = VoiceEffect.RouteTo(next),
            nextContext = DialogContext.ChoosingOption(places.size),
        )
    }

    private fun confirm(pending: VoiceEffect?): VoiceReply = when (pending) {
        is VoiceEffect.RouteTo -> routeTo(pending.place)
        VoiceEffect.ClearHistory -> VoiceReply("운전 기록을 지웠어요.", effect = VoiceEffect.ClearHistory)
        else -> VoiceReply("알겠어요.", effect = pending)
    }

    private fun clarify(reason: ClarificationReason): VoiceReply = VoiceReply(
        when (reason) {
            ClarificationReason.NOT_UNDERSTOOD -> "잘 못 들었어요. ‘차 상태 어때?’처럼 말해 보세요."
            ClarificationReason.NO_OPTIONS_SHOWN -> "지금은 고를 목록이 없어요. ‘가까운 블루핸즈 찾아 줘’처럼 말해 보세요."
            ClarificationReason.OPTION_OUT_OF_RANGE -> "목록에 없는 번호예요. 화면의 번호로 말해 주세요."
            ClarificationReason.NOTHING_TO_CONFIRM -> "무엇을 할지 다시 말해 주세요."
        },
    )

    private fun statusSentence(facts: VoiceFacts): String {
        val warning = facts.topWarning ?: return "모두 괜찮아요. ${rangeSentence(facts.fuel)}"
        return "${warning.oneLine}."
    }

    private fun keepDrivingSentence(warning: Explanation?): String = when (warning?.urgency) {
        null -> "네, 지금은 괜찮아요."
        Urgency.INFO -> "네, 달려도 돼요. 정차하면 ${KoreanParticles.withObject(warning.title)} 확인해 주세요."
        Urgency.SERVICE_SOON -> "당장 멈출 필요는 없지만 가까운 시일 안에 점검받으세요."
        Urgency.IMMEDIATE_STOP -> "안전한 곳에 세우는 게 좋아요."
    }

    private fun rangeSentence(fuel: RefuelAdvice): String = when (fuel) {
        RefuelAdvice.Unknown -> "연료 정보는 아직 없어요."
        is RefuelAdvice.Enough -> "지금 연료로 ${FuelText.rangeText(fuel.range)} 더 갈 수 있어요."
        is RefuelAdvice.RefuelOnTheWay -> "${FuelText.rangeText(fuel.range)} 갈 수 있어요. 가는 길에 주유하는 게 좋아요."
        is RefuelAdvice.RefuelNow -> "${FuelText.rangeText(fuel.range)}밖에 못 갈 수 있어요. 지금 주유하는 게 좋아요."
    }

    private fun reportSentence(facts: VoiceFacts): String {
        val events = facts.eventCounts.filterValues { it > 0 }.entries
            .joinToString(", ") { (type, count) -> "${type.label()} ${count}번" }
        return if (events.isEmpty()) "오늘은 ${facts.score}점이에요. 부드럽게 운전했어요." else "오늘은 ${facts.score}점이에요. $events 있었어요."
    }

    private fun PlaceCategory.spoken(): String = when (this) {
        PlaceCategory.BLUE_HANDS -> "블루핸즈"
        PlaceCategory.GAS_STATION -> "주유소"
    }
}
