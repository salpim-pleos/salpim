package com.salpim.feature.voice

/** 단어표 하나와 그 단어가 들리면 고르는 의도 */
internal data class KeywordRule(val intent: VoiceIntent, val keywords: List<String>) {
    fun matches(normalized: String): Boolean = keywords.any { normalized.contains(it) }
}

/**
 * 한국어 음성 명령 → [VoiceIntent]. 오프라인 단어표 방식이다 (LLM/네트워크 의존 금지, AGENTS.md 3절).
 *
 * 우선순위 [설계] — 위에서 먼저 걸린 것이 이긴다:
 * ```
 * 1. 기록 삭제          "기록 지워 줘"
 * 2. 번호 선택          "두 번째 주유소" → SelectOption(2)  (주유소보다 먼저)
 * 3. 다른 곳            "다음 주유소"    → NextOption        (주유소보다 먼저)
 * 4. 예약
 * 5. 정비소 / 6. 주유소 "주유소 들러 줘" → FindGasStation    (승낙보다 먼저)
 * 7. 남은 거리 / 8. 계속 주행 / 9. 리포트
 * 10. 이유              "왜 그래?"       → ExplainIssue      (승낙 "그래"보다 먼저)
 * 11. 차 상태
 * 12. 취소              "아니 다른 데"   → NextOption        (취소는 주제가 없을 때만)
 * 13. 승낙              "들러 줘"        → Confirm           (가장 마지막)
 * ```
 */
class VoiceCommandParser {
    fun parse(text: String): VoiceIntent {
        val normalized = VoiceText.normalize(text)
        if (normalized.isEmpty()) return VoiceIntent.Unknown(text)
        return CLEAR_HISTORY_RULE.intentIfMatches(normalized)
            ?: OrdinalParser.parse(normalized)?.let(VoiceIntent::SelectOption)
            ?: TOPIC_RULES.firstOrNull { it.matches(normalized) }?.intent
            ?: confirmOrNull(normalized)
            ?: VoiceIntent.Unknown(text)
    }

    private fun confirmOrNull(normalized: String): VoiceIntent? {
        val isConfirm = normalized in VoiceVocabulary.CONFIRM_WORDS ||
            VoiceVocabulary.CONFIRM_PHRASES.any { normalized.contains(it) }
        return if (isConfirm) VoiceIntent.Confirm else null
    }

    private fun KeywordRule.intentIfMatches(normalized: String): VoiceIntent? = intent.takeIf { matches(normalized) }

    private companion object {
        val CLEAR_HISTORY_RULE = KeywordRule(VoiceIntent.ClearHistory, VoiceVocabulary.CLEAR_HISTORY)

        /** 번호 선택 다음에 순서대로 검사한다. 순서를 바꾸면 우선순위 테스트를 같이 고친다 */
        val TOPIC_RULES = listOf(
            KeywordRule(VoiceIntent.NextOption, VoiceVocabulary.NEXT_OPTION),
            KeywordRule(VoiceIntent.OpenReservation, VoiceVocabulary.RESERVATION),
            KeywordRule(VoiceIntent.FindServiceCenter, VoiceVocabulary.SERVICE_CENTER),
            KeywordRule(VoiceIntent.FindGasStation, VoiceVocabulary.GAS_STATION),
            KeywordRule(VoiceIntent.AskRange, VoiceVocabulary.RANGE),
            KeywordRule(VoiceIntent.CanKeepDriving, VoiceVocabulary.KEEP_DRIVING),
            KeywordRule(VoiceIntent.AskReport, VoiceVocabulary.REPORT),
            KeywordRule(VoiceIntent.ExplainIssue, VoiceVocabulary.EXPLAIN),
            KeywordRule(VoiceIntent.CheckStatus, VoiceVocabulary.STATUS),
            KeywordRule(VoiceIntent.Cancel, VoiceVocabulary.CANCEL),
        )
    }
}
