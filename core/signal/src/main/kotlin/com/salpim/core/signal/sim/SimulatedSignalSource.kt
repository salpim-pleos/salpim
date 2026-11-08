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
                emitCurrent()
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
        val job = scope.launch {
            try {
                for (frame in frames) {
                    current = frame
                    emitCurrent()
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
