package com.salpim.core.signal.sim

import com.salpim.core.model.VehicleSignal
import com.salpim.core.signal.SignalSource
import com.salpim.core.signal.SignalSourceKind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 시나리오 재생 신호원. 단위 테스트, e2e 테스트, 백업 시연용.
 * 시나리오가 없을 때도 tickMs마다 현재 스냅샷을 다시 내보낸다.
 */
class SimulatedSignalSource(
    private val scope: CoroutineScope,
    private val clock: () -> Long = System::currentTimeMillis,
    private val tickMs: Long = 200,
) : SignalSource {
    override val kind = SignalSourceKind.SIMULATED

    private var current: VehicleSignal = Scenarios.parked(clock())
    private val _signals = MutableStateFlow(current)
    override val signals: StateFlow<VehicleSignal> = _signals.asStateFlow()

    private var tickJob: Job? = null
    private var playJob: Job? = null

    private val _playing = MutableStateFlow<String?>(null)

    /** 재생 중인 시나리오 id */
    val playing: StateFlow<String?> = _playing.asStateFlow()

    override fun start() {
        if (tickJob?.isActive == true) return
        tickJob = scope.launch {
            while (isActive) {
                // 재생 중에는 재생 루프만 내보낸다. 두 루프가 섞이면 시각이 엇갈려 속도 변화율이 튄다
                if (playJob?.isActive != true) emitCurrent()
                delay(tickMs)
            }
        }
    }

    override fun stop() {
        cancelPlayback()
        tickJob?.cancel()
        tickJob = null
    }

    /** 현재 상태를 직접 바꾼다 (e2e 테스트에서 사용). 진행 중인 시나리오는 멈춘다. */
    fun set(f: VehicleSignal.() -> VehicleSignal) {
        cancelPlayback()
        current = current.f()
        emitCurrent()
    }

    fun play(scenario: Scenario): Job {
        cancelPlayback()
        val frames = scenario.frames(tickMs, current)
        _playing.value = scenario.id
        val baseMs = clock()
        val firstMs = frames.firstOrNull()?.timestampMs ?: 0L
        val job = scope.launch {
            try {
                for (frame in frames) {
                    current = frame
                    // [설계] 시각은 시나리오 간격 그대로. delay가 밀려도(느린 기기) 급가속·급제동으로 오인하지 않게
                    _signals.value = frame.copy(timestampMs = baseMs + frame.timestampMs - firstMs)
                    delay(tickMs)
                }
            } finally {
                if (_playing.value == scenario.id) _playing.value = null
            }
        }
        playJob = job
        return job
    }

    fun reset() {
        cancelPlayback()
        current = Scenarios.parked(clock())
        emitCurrent()
    }

    private fun cancelPlayback() {
        playJob?.cancel()
        playJob = null
        _playing.value = null
    }

    private fun emitCurrent() {
        _signals.value = current.copy(timestampMs = clock())
    }
}
