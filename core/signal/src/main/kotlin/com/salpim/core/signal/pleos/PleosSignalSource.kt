package com.salpim.core.signal.pleos

import ai.pleos.playground.vehicle.Vehicle
import android.content.Context
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
 * Pleos Vehicle SDK(ai.pleos.playground:Vehicle 2.3.2) 신호원.
 * 패턴 [확인됨]: `Vehicle(context)` → `initialize()` → 도메인 리스너 등록 → 종료 시 해제 후 `release()`.
 * 리스너가 따로따로 값을 주므로 최신 값을 모아 두고 [tickMs]마다 스냅샷을 내보낸다 (상태머신 디바운스용).
 *
 * 다루지 않는 것: 차속·주차 브레이크(Vehicle SDK에 없음, S3), 계기판 경고등·타이어 공기압(서드파티 불가, S2).
 */
class PleosSignalSource(
    context: Context,
    private val scope: CoroutineScope,
    private val clock: () -> Long,
    private val tickMs: Long = DEFAULT_TICK_MS,
) : SignalSource {
    override val kind = SignalSourceKind.VEHICLE

    private val appContext = context.applicationContext
    private val readings = MutableStateFlow(PleosReadings())
    private val _signals = MutableStateFlow(VehicleSignal(timestampMs = clock()))
    override val signals: StateFlow<VehicleSignal> = _signals.asStateFlow()

    private var vehicle: Vehicle? = null
    private var subscriptions: PleosSubscriptions? = null
    private var tickJob: Job? = null

    override fun start() {
        if (vehicle != null) return
        val v = Vehicle(appContext).also { it.initialize() }
        subscriptions = PleosSubscriptions(v, readings).also { it.registerAll() }
        vehicle = v
        tickJob = scope.launch {
            while (isActive) {
                _signals.value = readings.value.toSignal(clock())
                delay(tickMs)
            }
        }
    }

    /** 비동기 콜백이 끝나기 전에 release하지 말라는 문서 권고에 따라 해제 → release 순서를 지킨다 */
    override fun stop() {
        tickJob?.cancel()
        tickJob = null
        subscriptions?.unregisterAll()
        subscriptions = null
        vehicle?.release()
        vehicle = null
    }

    private companion object {
        const val DEFAULT_TICK_MS = 200L
    }
}
