package com.salpim.feature.voice

/** 지금 화면이 음성으로 무엇을 받을 수 있는지 */
sealed interface DialogContext {
    /** 고를 목록도, 물어본 것도 없다 */
    data object Idle : DialogContext

    /** 번호 붙은 목록이 보인다 ("첫 번째로 가 줘") */
    data class ChoosingOption(val optionCount: Int) : DialogContext

    /** 앱이 "들를까요?"처럼 예/아니오를 물었다 */
    data object AwaitingConfirmation : DialogContext
}

enum class ClarificationReason {
    /** 알아듣지 못함 */
    NOT_UNDERSTOOD,

    /** 번호로 고르라고 했지만 보이는 목록이 없음 */
    NO_OPTIONS_SHOWN,

    /** 목록에 없는 번호 */
    OPTION_OUT_OF_RANGE,

    /** 승낙했지만 물어본 것이 없음 */
    NOTHING_TO_CONFIRM,
}

sealed interface VoiceResolution {
    data class Accepted(val intent: VoiceIntent) : VoiceResolution

    /** 앱이 되물어야 한다. 원문은 화면에 "이렇게 들었어요"로 보여 준다 */
    data class NeedsClarification(val reason: ClarificationReason, val heardText: String) : VoiceResolution
}

/**
 * 화면 맥락에 맞는 의도만 통과시킨다. 번호·다음·승낙은 고를 것이나 물어본 것이 있을 때만 뜻이 있다.
 * 취소는 언제나 받는다 (말하던 안내를 멈추는 용도로도 쓴다).
 */
class VoiceDialog(private val parser: VoiceCommandParser = VoiceCommandParser()) {
    fun resolve(text: String, context: DialogContext): VoiceResolution {
        val intent = parser.parse(text)
        val reason = clarificationReason(intent, context) ?: return VoiceResolution.Accepted(intent)
        return VoiceResolution.NeedsClarification(reason, text)
    }

    private fun clarificationReason(intent: VoiceIntent, context: DialogContext): ClarificationReason? = when (intent) {
        is VoiceIntent.Unknown -> ClarificationReason.NOT_UNDERSTOOD
        is VoiceIntent.SelectOption -> selectionReason(intent.ordinal, context)
        VoiceIntent.NextOption -> if (context is DialogContext.ChoosingOption) null else ClarificationReason.NO_OPTIONS_SHOWN
        VoiceIntent.Confirm -> if (context == DialogContext.Idle) ClarificationReason.NOTHING_TO_CONFIRM else null
        else -> null
    }

    private fun selectionReason(ordinal: Int, context: DialogContext): ClarificationReason? = when {
        context !is DialogContext.ChoosingOption -> ClarificationReason.NO_OPTIONS_SHOWN
        ordinal !in FIRST_ORDINAL..context.optionCount -> ClarificationReason.OPTION_OUT_OF_RANGE
        else -> null
    }

    private companion object {
        const val FIRST_ORDINAL = 1
    }
}
