package com.salpim.feature.voice

/**
 * 음성 명령 단어표. 모든 단어는 [VoiceText.normalize]를 거친 형태(공백·문장부호 없음, 라틴 소문자)로 적는다.
 * 문장 안에 단어가 들어 있으면 일치로 본다. 어떤 의도가 먼저인지는 [VoiceCommandParser]의 규칙 순서가 정한다.
 */
internal object VoiceVocabulary {
    val CLEAR_HISTORY = listOf("기록지워", "기록삭제", "기록없애", "기록다지워", "이력지워", "이력삭제")
    val NEXT_OPTION = listOf("다음", "다른데", "다른곳", "딴데")
    val RESERVATION = listOf("예약")
    val SERVICE_CENTER = listOf("블루핸즈", "정비소", "카센터", "서비스센터", "정비", "수리")
    val GAS_STATION = listOf("주유소", "기름넣")
    val RANGE = listOf("더갈수", "얼마나갈", "몇킬로", "몇km", "남은거리", "주행가능")
    val KEEP_DRIVING = listOf("계속달려", "계속가", "계속운전", "달려도돼", "운전해도돼", "가도돼")
    val REPORT = listOf("점수", "급제동", "급가속", "급출발", "리포트", "운전습관")
    val EXPLAIN = listOf("왜", "이유", "무슨뜻", "무슨의미")
    val STATUS = listOf("상태", "차어때", "차괜찮", "이상없", "이상있")
    val CANCEL = listOf("취소", "아니", "됐어", "그만", "싫어", "안할래")

    /** 문장 안에 있으면 승낙 */
    val CONFIRM_PHRASES = listOf("그래", "좋아", "들러줘", "가줘", "가자", "안내해줘", "그렇게해")

    /** 너무 짧아 다른 말 속에 섞이기 쉬우므로 문장 전체가 이 단어일 때만 승낙 */
    val CONFIRM_WORDS = setOf("응", "어", "네", "예", "넵", "웅", "오케이", "ok", "okay")

    /** 고유어 서수. "네 번째"는 승낙 "네"보다 먼저 판단된다 */
    val NATIVE_ORDINALS = mapOf(
        "첫번째" to 1,
        "첫째" to 1,
        "두번째" to 2,
        "둘째" to 2,
        "세번째" to 3,
        "셋째" to 3,
        "네번째" to 4,
        "넷째" to 4,
        "다섯번째" to 5,
        "다섯째" to 5,
    )

    /**
     * 한자어 번호. "이번 주 점수"처럼 일상어와 겹치므로 문장 맨 앞에 있고
     * 뒤에 [SINO_ORDINAL_TAILS] 중 하나만 올 때만 번호로 본다.
     */
    val SINO_ORDINALS = mapOf(
        "일번" to 1,
        "이번" to 2,
        "삼번" to 3,
        "사번" to 4,
        "오번" to 5,
    )

    val SINO_ORDINAL_TAILS = setOf("", "요", "이요", "으로", "로", "으로가줘", "으로해줘", "가줘", "주유소", "정비소", "블루핸즈")
}
