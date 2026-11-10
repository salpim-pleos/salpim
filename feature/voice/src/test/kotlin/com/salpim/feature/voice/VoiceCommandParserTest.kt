package com.salpim.feature.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceCommandParserTest {
    private val parser = VoiceCommandParser()

    private fun assertParses(expected: VoiceIntent, vararg utterances: String) {
        utterances.forEach { assertEquals("\"$it\"", expected, parser.parse(it)) }
    }

    // 정규화

    @Test
    fun destinationsAreRecognizedWithTheirName() {
        assertParses(VoiceIntent.GoTo("인천공항"), "인천공항 가 줘", "인천공항으로 가자", "인천공항까지 안내해 줘")
        assertParses(VoiceIntent.GoTo("서울시청"), "서울 시청으로 가 줘", "서울시청 가고 싶어")
    }

    @Test
    fun destinationRuleDoesNotStealOtherIntents() {
        assertParses(VoiceIntent.FindServiceCenter, "블루핸즈로 가 줘")
        assertParses(VoiceIntent.SelectOption(1), "첫 번째로 가 줘")
        assertParses(VoiceIntent.Confirm, "가 줘", "거기로 가 줘", "응 가자")
        assertParses(VoiceIntent.CanKeepDriving, "계속 가도 돼?")
    }

    @Test
    fun whereToRefuelAsksForGasStations() {
        assertParses(VoiceIntent.FindGasStation, "어디서 넣어?", "어디서 주유해야 돼?")
    }

    @Test
    fun normalizeRemovesSpacesPunctuationAndLowercasesLatin() {
        assertEquals("차상태어때ok", VoiceText.normalize("  차 상태, 어때?! OK "))
    }

    @Test
    fun spacingAndPunctuationVariantsGiveSameIntent() {
        assertParses(VoiceIntent.CheckStatus, "차 상태 어때", "차상태어때?", " 차 상태 어때요... ", "차 어때?")
    }

    // 의도별

    @Test
    fun keepDrivingQuestionsAreRecognized() {
        assertParses(VoiceIntent.CanKeepDriving, "계속 달려도 돼?", "이대로 계속 가도 돼?", "운전해도 돼?")
    }

    @Test
    fun whyQuestionsAskForExplanation() {
        assertParses(VoiceIntent.ExplainIssue, "왜 그래?", "왜 부족해?", "이유가 뭐야", "그게 무슨 뜻이야")
    }

    @Test
    fun serviceCenterSynonymsAreRecognized() {
        assertParses(VoiceIntent.FindServiceCenter, "가까운 블루핸즈 찾아 줘", "정비소", "카센터 어디 있어", "수리 맡길 데")
    }

    @Test
    fun gasStationRequestsAreRecognized() {
        assertParses(VoiceIntent.FindGasStation, "가까운 주유소", "주유소 찾아 줘", "기름 넣으러 가자")
    }

    @Test
    fun rangeQuestionsAreRecognized() {
        assertParses(VoiceIntent.AskRange, "얼마나 더 갈 수 있어?", "몇 킬로 남았어?", "남은 거리", "주유 안 하고 얼마나 갈 수 있어")
    }

    @Test
    fun reservationRequestsAreRecognized() {
        assertParses(VoiceIntent.OpenReservation, "예약할래", "예약해 줘")
    }

    @Test
    fun reportQuestionsAreRecognized() {
        assertParses(VoiceIntent.AskReport, "오늘 점수", "급제동은 어디서 했어?", "급가속 몇 번 했어", "운전 습관 알려 줘")
    }

    @Test
    fun clearHistoryRequestsAreRecognized() {
        assertParses(VoiceIntent.ClearHistory, "기록 지워 줘", "기록 삭제", "기록 다 지워")
    }

    @Test
    fun nextOptionRequestsAreRecognized() {
        assertParses(VoiceIntent.NextOption, "다음 주유소", "다른 데", "다른 곳 없어?")
    }

    @Test
    fun confirmPhrasesAreRecognized() {
        assertParses(VoiceIntent.Confirm, "응", "그래", "들러 줘", "좋아", "네", "응 그렇게 해", "OK")
    }

    @Test
    fun cancelPhrasesAreRecognized() {
        assertParses(VoiceIntent.Cancel, "아니", "됐어", "취소", "아니요", "그만")
    }

    // 서수

    @Test
    fun nativeOrdinalsSelectOption() {
        assertParses(VoiceIntent.SelectOption(1), "첫 번째로 가 줘", "첫째")
        assertParses(VoiceIntent.SelectOption(2), "두 번째", "둘째로")
        assertParses(VoiceIntent.SelectOption(3), "세 번째", "셋째")
        assertParses(VoiceIntent.SelectOption(4), "네 번째로 가 줘")
        assertParses(VoiceIntent.SelectOption(5), "다섯 번째")
    }

    @Test
    fun digitOrdinalsSelectOption() {
        assertParses(VoiceIntent.SelectOption(3), "3번", "3번으로 가 줘", "3 번째")
        assertParses(VoiceIntent.SelectOption(1), "1번")
    }

    @Test
    fun digitOrdinalBeyondListIsStillParsedForDialogToReject() {
        assertParses(VoiceIntent.SelectOption(7), "7번")
    }

    @Test
    fun sinoOrdinalsSelectOptionWhenSpokenAlone() {
        assertParses(VoiceIntent.SelectOption(1), "일 번")
        assertParses(VoiceIntent.SelectOption(2), "이번으로 가 줘")
        assertParses(VoiceIntent.SelectOption(3), "삼번", "삼 번 주유소")
    }

    @Test
    fun everydayIbeonIsNotTreatedAsOrdinal() {
        assertParses(VoiceIntent.AskReport, "이번 주 점수")
        assertParses(VoiceIntent.Unknown("이번엔 뭐야"), "이번엔 뭐야")
    }

    // 우선순위 충돌

    @Test
    fun gasStationWithVisitBeatsConfirm() {
        assertParses(VoiceIntent.FindGasStation, "주유소 들러 줘")
        assertParses(VoiceIntent.Confirm, "들러 줘")
    }

    @Test
    fun ordinalBeatsGasStation() {
        assertParses(VoiceIntent.SelectOption(2), "두 번째 주유소", "두 번째 주유소 들러 줘")
    }

    @Test
    fun ordinalNeBeatsConfirmNe() {
        assertParses(VoiceIntent.SelectOption(4), "네 번째")
        assertParses(VoiceIntent.Confirm, "네")
    }

    @Test
    fun nextBeatsGasStationAndCancel() {
        assertParses(VoiceIntent.NextOption, "다음 주유소", "아니 다른 데")
    }

    @Test
    fun whyBeatsConfirmGeurae() {
        assertParses(VoiceIntent.ExplainIssue, "왜 그래")
        assertParses(VoiceIntent.Confirm, "그래")
    }

    @Test
    fun serviceCenterWithGoBeatsConfirm() {
        assertParses(VoiceIntent.FindServiceCenter, "블루핸즈로 가 줘")
    }

    @Test
    fun clearHistoryBeatsReport() {
        assertParses(VoiceIntent.ClearHistory, "급제동 기록 지워 줘")
    }

    @Test
    fun topicBeatsCancel() {
        assertParses(VoiceIntent.FindServiceCenter, "아니 정비소 찾아 줘")
    }

    @Test
    fun shortConfirmWordInsideOtherSpeechIsNotConfirm() {
        assertParses(VoiceIntent.Unknown("어디야"), "어디야")
    }

    // 알 수 없음

    @Test
    fun unrelatedSpeechIsUnknownWithOriginalText() {
        assertParses(VoiceIntent.Unknown("노래 틀어 줘"), "노래 틀어 줘")
    }

    @Test
    fun blankSpeechIsUnknown() {
        assertParses(VoiceIntent.Unknown("  ?! "), "  ?! ")
        assertParses(VoiceIntent.Unknown(""), "")
    }
}
