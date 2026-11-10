package com.salpim.app.voice

import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.feature.fuel.RefuelAdvice
import com.salpim.feature.service.PlaceCategory
import com.salpim.feature.voice.DialogContext

/** 앱이 먼저 말을 걸지 판단하는 데 필요한 순간 상태 */
data class NoticeSnapshot(val activeWarnings: List<Explanation>, val refuel: RefuelAdvice)

/**
 * 앱이 먼저 거는 한 줄 (AGENTS.md 3절): 차량 이상 경고, 그리고 주유가 필요해진 순간뿐이다.
 * 상태가 "새로" 바뀐 순간에만 한 번 말한다 — 같은 경고·같은 판단을 되풀이하지 않는다.
 * 우선순위: 경고 > 주유. 안내(INFO) 경고는 먼저 말하지 않는다.
 */
object ProactiveNotice {
    fun between(previous: NoticeSnapshot, current: NoticeSnapshot): VoiceReply? =
        newWarning(previous, current) ?: refuelNeeded(previous, current)

    private fun newWarning(previous: NoticeSnapshot, current: NoticeSnapshot): VoiceReply? {
        val before = previous.activeWarnings.map { it.code }.toSet()
        val fresh = current.activeWarnings
            .filter { it.code !in before && it.urgency != Urgency.INFO }
            .minByOrNull { it.urgency.ordinal }
            ?: return null
        return VoiceReply(sentence(fresh.oneLine))
    }

    private fun refuelNeeded(previous: NoticeSnapshot, current: NoticeSnapshot): VoiceReply? {
        if (previous.refuel.showsStations || !current.refuel.showsStations) return null
        val text = when (current.refuel) {
            is RefuelAdvice.RefuelNow -> "연료가 부족해요. 지금 주유하는 게 좋아요. 주유소를 찾아 드릴까요?"
            else -> "이대로 가면 가는 길에 주유해야 해요. 주유소를 찾아 드릴까요?"
        }
        return VoiceReply(
            text,
            pending = VoiceEffect.FindPlaces(PlaceCategory.GAS_STATION),
            nextContext = DialogContext.AwaitingConfirmation,
        )
    }

    private fun sentence(text: String): String = text.trimEnd('.') + "."
}
