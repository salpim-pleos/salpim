package com.salpim.feature.voice.android

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.salpim.feature.voice.SpeechErrorReason
import com.salpim.feature.voice.SpeechEvent
import com.salpim.feature.voice.SpeechInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Android 표준 [SpeechRecognizer] 기반 음성 입력. 온디바이스 인식기가 있으면 그것을 쓴다
 * (주행 중 네트워크가 불안정하므로, AGENTS.md 3절 규칙 4).
 *
 * **[startListening], [stopListening], [release]는 메인 스레드에서만 부른다.**
 * `SpeechRecognizer`는 메인 스레드에서 만들고 호출해야 하며, 결과 콜백도 메인 스레드로 온다.
 * `RECORD_AUDIO` 런타임 권한은 화면 쪽에서 미리 받아 둔다 (없으면 [SpeechErrorReason.PERMISSION_DENIED]).
 *
 * TODO(spike-S6): Pleos SpeechToText SDK(`ai.pleos.playground:SpeechToText:3.3.1`, ON_DEVICE 모드)로 바꿀지 검토.
 * 바꾸면 이 클래스만 [SpeechInput]의 다른 구현으로 갈아 끼운다. SDK 클래스 이름은 문서 확인 전까지 쓰지 않는다.
 */
class AndroidSpeechInput(private val context: Context) : SpeechInput {
    private val mutableEvents = MutableSharedFlow<SpeechEvent>(extraBufferCapacity = EVENT_BUFFER_CAPACITY)
    override val events: Flow<SpeechEvent> = mutableEvents.asSharedFlow()

    private var recognizer: SpeechRecognizer? = null

    override fun startListening() {
        checkMainThread()
        val active = recognizer ?: createRecognizer()?.also { recognizer = it }
        if (active == null) {
            mutableEvents.tryEmit(SpeechEvent.Error(SpeechErrorReason.UNAVAILABLE))
            return
        }
        active.startListening(recognizerIntent())
    }

    override fun stopListening() {
        checkMainThread()
        recognizer?.stopListening()
    }

    /** 화면이 사라질 때 인식기를 정리한다 */
    fun release() {
        checkMainThread()
        recognizer?.destroy()
        recognizer = null
    }

    private fun createRecognizer(): SpeechRecognizer? {
        val created = when {
            isOnDeviceAvailable() -> SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            SpeechRecognizer.isRecognitionAvailable(context) -> SpeechRecognizer.createSpeechRecognizer(context)
            else -> null
        }
        created?.setRecognitionListener(Listener())
        return created
    }

    private fun isOnDeviceAvailable(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    private fun recognizerIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANGUAGE_TAG)
        .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)

    private fun checkMainThread() {
        check(Looper.myLooper() == Looper.getMainLooper()) { "AndroidSpeechInput은 메인 스레드에서만 호출한다" }
    }

    private inner class Listener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            mutableEvents.tryEmit(SpeechEvent.Listening)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            firstText(partialResults)?.let { mutableEvents.tryEmit(SpeechEvent.PartialText(it)) }
        }

        override fun onResults(results: Bundle?) {
            val event = firstText(results)?.let(SpeechEvent::FinalText) ?: SpeechEvent.Error(SpeechErrorReason.NO_MATCH)
            mutableEvents.tryEmit(event)
        }

        override fun onError(error: Int) {
            mutableEvents.tryEmit(SpeechEvent.Error(errorReason(error)))
        }

        override fun onBeginningOfSpeech() {
            // 듣는 중 표시는 onReadyForSpeech에서 이미 했다
        }

        override fun onRmsChanged(rmsdB: Float) {
            // 음량 막대는 그리지 않는다 (주행 중 움직임 최소, DESIGN.md)
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // 원음은 쓰지 않는다 (개인정보 최소화)
        }

        override fun onEndOfSpeech() {
            // 결과는 onResults로 온다
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // 예약된 확장 이벤트, 쓰지 않음
        }
    }

    private companion object {
        const val LANGUAGE_TAG = "ko-KR"
        const val EVENT_BUFFER_CAPACITY = 16

        fun firstText(bundle: Bundle?): String? = bundle
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull { it.isNotBlank() }

        fun errorReason(error: Int): SpeechErrorReason = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> SpeechErrorReason.NO_MATCH
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechErrorReason.SPEECH_TIMEOUT
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechErrorReason.PERMISSION_DENIED
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> SpeechErrorReason.BUSY
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT, SpeechRecognizer.ERROR_SERVER ->
                SpeechErrorReason.NETWORK
            else -> SpeechErrorReason.OTHER
        }
    }
}
