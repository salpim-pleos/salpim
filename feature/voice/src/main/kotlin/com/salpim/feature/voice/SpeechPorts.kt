package com.salpim.feature.voice

import kotlinx.coroutines.flow.Flow

/** 음성 인식 진행 상황 */
sealed interface SpeechEvent {
    /** 마이크가 열려 듣는 중 (음성 바 마이크를 파랑으로) */
    data object Listening : SpeechEvent

    /** 말하는 도중의 중간 결과. 화면에 흐리게 보여 주기만 하고 명령으로 쓰지 않는다 */
    data class PartialText(val text: String) : SpeechEvent

    /** 말이 끝난 최종 결과. 이것만 [VoiceCommandParser]로 넘긴다 */
    data class FinalText(val text: String) : SpeechEvent

    data class Error(val reason: SpeechErrorReason) : SpeechEvent
}

enum class SpeechErrorReason {
    /** 들었지만 글자로 바꾸지 못함 */
    NO_MATCH,

    /** 말이 없었음 */
    SPEECH_TIMEOUT,

    /** 마이크 권한 없음 */
    PERMISSION_DENIED,

    /** 인식기가 다른 요청을 처리 중 */
    BUSY,

    /** 이 기기에 인식기가 없음 */
    UNAVAILABLE,

    /** 네트워크 인식기의 연결 문제 (온디바이스 인식기면 생기지 않아야 함) */
    NETWORK,

    OTHER,
}

/** 음성 입력 포트. 구현은 Android STT 또는 Pleos STT SDK (TODO(spike-S6)) */
interface SpeechInput {
    val events: Flow<SpeechEvent>

    fun startListening()

    fun stopListening()
}

/** 말하는 이유. 경고는 하던 대답을 끊고 먼저 말한다 */
enum class SpeechPriority {
    WARNING,
    ANSWER,
}

/** 음성 출력 포트. 대답은 항상 화면 글자로도 남긴다 (AGENTS.md 3절 규칙 3) */
interface SpeechOutput {
    /** 말을 마칠 때까지 기다린다. 취소되면 말을 멈춘다 */
    suspend fun speak(text: String, priority: SpeechPriority)

    fun stop()
}
