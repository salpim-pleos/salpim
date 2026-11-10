package com.salpim.feature.voice

/** 사용자가 말로 요청한 것. 화면 버튼과 같은 동작에 연결된다 (음성이 주 조작 방식, AGENTS.md 3절). */
sealed interface VoiceIntent {
    /** "차 상태 어때?" */
    data object CheckStatus : VoiceIntent

    /** "계속 달려도 돼?" */
    data object CanKeepDriving : VoiceIntent

    /** "왜 그래?", "왜 부족해?" */
    data object ExplainIssue : VoiceIntent

    /** "가까운 블루핸즈 찾아 줘", "정비소" */
    data object FindServiceCenter : VoiceIntent

    /** "가까운 주유소", "주유소 들러 줘" */
    data object FindGasStation : VoiceIntent

    /** "얼마나 더 갈 수 있어?" */
    data object AskRange : VoiceIntent

    /** "첫 번째로 가 줘", "3번". [ordinal]은 1부터 센다 (화면에 붙은 번호와 같다) */
    data class SelectOption(val ordinal: Int) : VoiceIntent

    /** "다음 주유소", "다른 데" */
    data object NextOption : VoiceIntent

    /** "인천공항 가 줘", "서울시청으로 가자" — 정비소·주유소가 아닌 목적지. [destination]은 공백 없는 장소 이름 */
    data class GoTo(val destination: String) : VoiceIntent

    /** "예약할래", "예약해 줘" */
    data object OpenReservation : VoiceIntent

    /** "오늘 점수", "급제동은 어디서 했어?" */
    data object AskReport : VoiceIntent

    /** "기록 지워 줘" */
    data object ClearHistory : VoiceIntent

    /** "응", "그래", "들러 줘", "좋아" */
    data object Confirm : VoiceIntent

    /** "아니", "됐어", "취소" */
    data object Cancel : VoiceIntent

    /** 알아듣지 못한 말. 원문은 화면에 "이렇게 들었어요"로 보여 줄 때 쓴다 */
    data class Unknown(val text: String) : VoiceIntent
}
