package com.salpim.feature.voice

import java.util.Locale

/** 인식 결과 정규화. STT는 띄어쓰기·문장부호를 일정하게 주지 않으므로 글자와 숫자만 남긴다 */
object VoiceText {
    private val NON_LETTER_OR_DIGIT = Regex("[^\\p{L}\\p{N}]")

    fun normalize(text: String): String = text.trim().lowercase(Locale.ROOT).replace(NON_LETTER_OR_DIGIT, "")
}
