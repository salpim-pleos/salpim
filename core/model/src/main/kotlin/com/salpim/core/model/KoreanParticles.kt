package com.salpim.core.model

/**
 * 이름 뒤에 붙는 조사를 받침에 맞춰 고른다. 장소·경고 이름이 데이터에서 오므로 "(으)로" 같은 병기 대신 쓴다.
 *
 * 받침은 마지막 한글 음절(또는 숫자)로 판단한다. 끝의 괄호·공백·문장부호는 건너뛴다
 * ("재동주유소 (현대오일뱅크)" → "크"). 판단할 글자가 없으면 받침 없음으로 본다.
 */
object KoreanParticles {
    private const val HANGUL_FIRST = 0xAC00
    private const val HANGUL_LAST = 0xD7A3
    private const val FINAL_CONSONANTS = 28
    private const val FINAL_RIEUL = 8

    /** 받침 있는 숫자 읽기: 영(공)·일·삼·육·칠·팔 */
    private val DIGITS_WITH_FINAL = setOf('0', '1', '3', '6', '7', '8')

    /** ㄹ 받침 숫자 읽기: 일·칠·팔 — "으로"가 아니라 "로" */
    private val DIGITS_WITH_RIEUL = setOf('1', '7', '8')

    /** 으로/로: 받침 없거나 ㄹ 받침이면 "로" */
    fun withDirection(word: String): String = word + if (final(word).takesEuro) "으로" else "로"

    /** 을/를 */
    fun withObject(word: String): String = word + if (final(word).hasFinal) "을" else "를"

    /** 이/가 */
    fun withSubject(word: String): String = word + if (final(word).hasFinal) "이" else "가"

    /** 은/는 */
    fun withTopic(word: String): String = word + if (final(word).hasFinal) "은" else "는"

    private data class Final(val hasFinal: Boolean, val isRieul: Boolean) {
        val takesEuro: Boolean get() = hasFinal && !isRieul
    }

    private fun final(word: String): Final {
        val last = word.lastOrNull { it.isLetterOrDigit() } ?: return Final(hasFinal = false, isRieul = false)
        return when {
            last.code in HANGUL_FIRST..HANGUL_LAST -> {
                val index = (last.code - HANGUL_FIRST) % FINAL_CONSONANTS
                Final(hasFinal = index != 0, isRieul = index == FINAL_RIEUL)
            }
            last.isDigit() -> Final(hasFinal = last in DIGITS_WITH_FINAL, isRieul = last in DIGITS_WITH_RIEUL)
            else -> Final(hasFinal = false, isRieul = false)
        }
    }
}
