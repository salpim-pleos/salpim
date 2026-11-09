package com.salpim.app.voice

import com.salpim.core.designsystem.VoicePhase
import com.salpim.feature.voice.SpeechEvent
import com.salpim.feature.voice.SpeechInput
import com.salpim.feature.voice.SpeechOutput
import com.salpim.feature.voice.SpeechPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 마이크와 스피커를 묶어 음성 바 상태를 만든다. 들은 최종 문장은 [onHeard]로 넘긴다.
 * 인식·합성 엔진은 포트 뒤에 있다 (Android 기본 → TODO(spike-S6): Pleos STT/TTS SDK).
 */
class VoiceController(
    private val scope: CoroutineScope,
    private val input: SpeechInput,
    private val output: SpeechOutput,
    private val onHeard: (String) -> Unit,
) {
    private val _phase = MutableStateFlow(VoicePhase.IDLE)
    val phase: StateFlow<VoicePhase> = _phase.asStateFlow()

    private val _partialText = MutableStateFlow<String?>(null)

    /** 말하는 도중 들린 글자 (흐리게 보여 주기만 한다) */
    val partialText: StateFlow<String?> = _partialText.asStateFlow()

    private var speakJob: Job? = null

    init {
        scope.launch { input.events.collect(::onEvent) }
    }

    fun toggleListening() {
        if (_phase.value == VoicePhase.LISTENING) {
            input.stopListening()
            _phase.value = VoicePhase.IDLE
        } else {
            stopSpeaking()
            input.startListening()
        }
    }

    fun say(text: String, priority: SpeechPriority = SpeechPriority.ANSWER) {
        speakJob?.cancel()
        speakJob = scope.launch {
            _phase.value = VoicePhase.SPEAKING
            try {
                output.speak(text, priority)
            } finally {
                if (_phase.value == VoicePhase.SPEAKING) _phase.value = VoicePhase.IDLE
            }
        }
    }

    fun stopSpeaking() {
        speakJob?.cancel()
        output.stop()
    }

    private fun onEvent(event: SpeechEvent) {
        when (event) {
            SpeechEvent.Listening -> _phase.value = VoicePhase.LISTENING
            is SpeechEvent.PartialText -> _partialText.value = event.text
            is SpeechEvent.FinalText -> finish { onHeard(event.text) }
            is SpeechEvent.Error -> finish { onHeard("") }
        }
    }

    private fun finish(then: () -> Unit) {
        _phase.value = VoicePhase.IDLE
        _partialText.value = null
        then()
    }
}
