package com.salpim.feature.voice

/** 정규화된 문장에서 목록 번호(1부터)를 찾는다. 범위 검사는 화면 목록을 아는 [VoiceDialog]가 한다 */
internal object OrdinalParser {
    /** "3번", "12번" — 숫자 뒤 "번째"도 여기에 걸린다 */
    private val DIGIT_ORDINAL = Regex("(\\d{1,2})번")

    fun parse(normalized: String): Int? = nativeOrdinal(normalized) ?: digitOrdinal(normalized) ?: sinoOrdinal(normalized)

    private fun nativeOrdinal(normalized: String): Int? = VoiceVocabulary.NATIVE_ORDINALS.entries
        .firstOrNull { (word, _) -> normalized.contains(word) }
        ?.value

    private fun digitOrdinal(normalized: String): Int? = DIGIT_ORDINAL.find(normalized)
        ?.groupValues
        ?.get(1)
        ?.toIntOrNull()

    private fun sinoOrdinal(normalized: String): Int? = VoiceVocabulary.SINO_ORDINALS.entries
        .firstOrNull { (word, _) ->
            normalized.startsWith(word) && normalized.removePrefix(word) in VoiceVocabulary.SINO_ORDINAL_TAILS
        }
        ?.value
}
