package com.salpim.feature.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceDialogTest {
    private val dialog = VoiceDialog()
    private val threeOptions = DialogContext.ChoosingOption(optionCount = 3)

    @Test
    fun selectionWithinShownListIsAccepted() {
        assertEquals(VoiceResolution.Accepted(VoiceIntent.SelectOption(3)), dialog.resolve("세 번째", threeOptions))
    }

    @Test
    fun selectionBeyondShownListAsksAgain() {
        assertEquals(
            VoiceResolution.NeedsClarification(ClarificationReason.OPTION_OUT_OF_RANGE, "네 번째"),
            dialog.resolve("네 번째", threeOptions),
        )
    }

    @Test
    fun selectionWithoutListAsksAgain() {
        assertEquals(
            VoiceResolution.NeedsClarification(ClarificationReason.NO_OPTIONS_SHOWN, "첫 번째"),
            dialog.resolve("첫 번째", DialogContext.Idle),
        )
    }

    @Test
    fun nextOptionNeedsList() {
        assertEquals(VoiceResolution.Accepted(VoiceIntent.NextOption), dialog.resolve("다른 데", threeOptions))
        assertEquals(
            VoiceResolution.NeedsClarification(ClarificationReason.NO_OPTIONS_SHOWN, "다른 데"),
            dialog.resolve("다른 데", DialogContext.AwaitingConfirmation),
        )
    }

    @Test
    fun confirmOnlyWhenSomethingWasAsked() {
        assertEquals(
            VoiceResolution.Accepted(VoiceIntent.Confirm),
            dialog.resolve("응", DialogContext.AwaitingConfirmation),
        )
        assertEquals(VoiceResolution.Accepted(VoiceIntent.Confirm), dialog.resolve("들러 줘", threeOptions))
        assertEquals(
            VoiceResolution.NeedsClarification(ClarificationReason.NOTHING_TO_CONFIRM, "응"),
            dialog.resolve("응", DialogContext.Idle),
        )
    }

    @Test
    fun cancelIsAlwaysAccepted() {
        assertEquals(VoiceResolution.Accepted(VoiceIntent.Cancel), dialog.resolve("취소", DialogContext.Idle))
    }

    @Test
    fun topicIntentsIgnoreContext() {
        assertEquals(
            VoiceResolution.Accepted(VoiceIntent.FindGasStation),
            dialog.resolve("주유소 들러 줘", DialogContext.Idle),
        )
    }

    @Test
    fun unknownSpeechAsksAgainWithHeardText() {
        assertEquals(
            VoiceResolution.NeedsClarification(ClarificationReason.NOT_UNDERSTOOD, "노래 틀어 줘"),
            dialog.resolve("노래 틀어 줘", threeOptions),
        )
    }
}
