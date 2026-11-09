package com.salpim.feature.voice.android

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.salpim.feature.voice.SpeechOutput
import com.salpim.feature.voice.SpeechPriority
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

/**
 * Android 표준 [TextToSpeech] 기반 음성 출력 (한국어). 말하는 동안만 오디오 포커스를 잡는다.
 *
 * 포커스는 `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` + `USAGE_ASSISTANT`로 요청한다 [설계]:
 * 사용자가 물어서 답하는 음성이라 음성 비서 용도가 가장 가깝고, 내비 안내(`USAGE_ASSISTANCE_NAVIGATION_GUIDANCE`)와
 * 같은 용도로 섞이지 않게 한다. 포커스를 못 받으면 말하지 않는다 (대답은 화면 글자로 이미 보인다).
 * TODO(audio-focus): Pleos Audio Focus 가이드 확인 — 용도(usage)·포커스 종류·내비 음성과의 우선순위는 확인 전이다.
 * 확인 전까지 앱이 먼저 말하는 경고([SpeechPriority.WARNING]) 호출은 앱 쪽에서 끈다 (AGENTS.md 3절 규칙 3).
 *
 * TODO(spike-S6): Pleos TextToSpeech SDK(`ai.pleos.playground:TextToSpeech:3.3.1`, ON_DEVICE 모드)로 바꿀지 검토.
 * 바꾸면 [SpeechOutput]의 다른 구현으로 갈아 끼운다. SDK 클래스 이름은 문서 확인 전까지 쓰지 않는다.
 */
class AndroidSpeechOutput(context: Context) : SpeechOutput {
    private val audioManager: AudioManager = context.getSystemService(AudioManager::class.java)
    private val attributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANT)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
    private val focusRequest: AudioFocusRequest = AudioFocusRequest.Builder(FOCUS_GAIN)
        .setAudioAttributes(attributes)
        .build()

    private val ready = CompletableDeferred<Boolean>()
    private val pendingUtterances = ConcurrentHashMap<String, CancellableContinuation<Unit>>()
    private val focusLock = Any()
    private var focusHolders = 0

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext, ::onInit)

    /** 엔진이 없거나 한국어 음성이 없으면 소리 없이 돌아온다 */
    override suspend fun speak(text: String, priority: SpeechPriority) {
        if (!ready.await() || !acquireFocus()) return
        try {
            speakAndAwait(text, priority)
        } finally {
            releaseFocus()
        }
    }

    override fun stop() {
        tts.stop()
    }

    /** 화면/서비스가 끝날 때 엔진을 정리한다 */
    fun release() {
        tts.shutdown()
    }

    private fun onInit(status: Int) {
        val configured = status == TextToSpeech.SUCCESS && configureEngine()
        ready.complete(configured)
    }

    private fun configureEngine(): Boolean {
        tts.setAudioAttributes(attributes)
        tts.setOnUtteranceProgressListener(ProgressListener())
        return tts.setLanguage(Locale.KOREAN) >= TextToSpeech.LANG_AVAILABLE
    }

    private suspend fun speakAndAwait(text: String, priority: SpeechPriority) = suspendCancellableCoroutine { continuation ->
        val utteranceId = UUID.randomUUID().toString()
        pendingUtterances[utteranceId] = continuation
        continuation.invokeOnCancellation {
            pendingUtterances.remove(utteranceId)
            tts.stop()
        }
        if (tts.speak(text, queueMode(priority), null, utteranceId) == TextToSpeech.ERROR) {
            finishUtterance(utteranceId)
        }
    }

    private fun finishUtterance(utteranceId: String?) {
        pendingUtterances.remove(utteranceId ?: return)?.resume(Unit)
    }

    /** 경고는 하던 대답을 끊는다. 끊긴 대답은 onStop으로 끝난다 */
    private fun queueMode(priority: SpeechPriority): Int = when (priority) {
        SpeechPriority.WARNING -> TextToSpeech.QUEUE_FLUSH
        SpeechPriority.ANSWER -> TextToSpeech.QUEUE_ADD
    }

    /** 여러 말이 겹쳐도 포커스는 첫 말에서 잡고 마지막 말이 끝날 때 놓는다 */
    private fun acquireFocus(): Boolean = synchronized(focusLock) {
        if (focusHolders == 0 && audioManager.requestAudioFocus(focusRequest) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return false
        }
        focusHolders++
        true
    }

    private fun releaseFocus() = synchronized(focusLock) {
        focusHolders--
        if (focusHolders == 0) audioManager.abandonAudioFocusRequest(focusRequest)
    }

    private inner class ProgressListener : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            // 시작 시점은 쓰지 않는다
        }

        override fun onDone(utteranceId: String?) = finishUtterance(utteranceId)

        override fun onStop(utteranceId: String?, interrupted: Boolean) = finishUtterance(utteranceId)

        override fun onError(utteranceId: String?, errorCode: Int) = finishUtterance(utteranceId)

        @Deprecated("API 21부터 onError(String, Int)가 불린다. 추상 메서드라 구현만 둔다")
        override fun onError(utteranceId: String?) = finishUtterance(utteranceId)
    }

    private companion object {
        const val FOCUS_GAIN = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
    }
}
