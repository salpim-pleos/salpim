package com.salpim.app

import com.salpim.core.data.HistoryRepository
import com.salpim.core.model.DrivingEvent
import com.salpim.core.model.DrivingState
import com.salpim.core.model.Explanation
import com.salpim.core.model.UxRestrictionFlags
import com.salpim.core.model.VehicleSignal
import com.salpim.core.model.WarningCode
import com.salpim.core.signal.SignalSource
import com.salpim.core.state.DrivingStateConfig
import com.salpim.core.state.DrivingStateMachine
import com.salpim.core.ux.UxRestrictionsSource
import com.salpim.feature.coach.HabitDetector
import com.salpim.feature.fuel.FuelMonitor
import com.salpim.feature.fuel.RefuelAdvice
import com.salpim.feature.warning.WarningExplainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class EngineState(
    val drivingState: DrivingState = DrivingState.PARKED,
    val ux: UxRestrictionFlags = UxRestrictionFlags.NONE,
    val signal: VehicleSignal? = null,
    /** 현재 활성 경고 (긴급도순) */
    val activeWarnings: List<Explanation> = emptyList(),
    /** 활성/과거 포함 기록된 경고 코드 */
    val seenWarnings: List<WarningCode> = emptyList(),
    val events: List<DrivingEvent> = emptyList(),
    val refuel: RefuelAdvice = RefuelAdvice.Unknown,
)

/** 신호 하나를 받아 해석하는 도메인 분석기 묶음 */
data class SignalAnalyzers(
    val explainer: WarningExplainer,
    val habitDetector: HabitDetector,
    val fuelMonitor: FuelMonitor,
)

/**
 * SignalSource → DrivingStateMachine → WarningExplainer / HabitDetector / History 파이프라인.
 * UI와 무관한 앱 상태를 한곳에서 만든다.
 */
class SalpimEngine(
    private val scope: CoroutineScope,
    private val signalSource: SignalSource,
    private val uxSource: UxRestrictionsSource,
    stateConfig: DrivingStateConfig,
    private val analyzers: SignalAnalyzers,
    private val history: HistoryRepository,
) {
    private val explainer get() = analyzers.explainer
    private val habitDetector get() = analyzers.habitDetector
    private val fuelMonitor get() = analyzers.fuelMonitor

    private val machine = DrivingStateMachine(stateConfig)
    private val _state = MutableStateFlow(EngineState())
    val state: StateFlow<EngineState> = _state.asStateFlow()

    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        signalSource.start()
        uxSource.start()
        job = scope.launch {
            combine(signalSource.signals, uxSource.flags) { s, f -> s to f }
                .collect { (s, f) -> onSignal(s, f) }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        signalSource.stop()
        uxSource.stop()
    }

    /** 로컬 기록 전체 삭제 (안전/UX 규칙 5) */
    fun clearHistory() {
        history.clearAll()
        _state.value = _state.value.copy(seenWarnings = currentSeen(), events = emptyList())
    }

    fun reset() {
        machine.reset()
        habitDetector.reset()
        history.clearAll()
        _state.value = EngineState()
    }

    private fun onSignal(signal: VehicleSignal, flags: UxRestrictionFlags) {
        val driving = machine.update(signal)
        signal.activeWarnings.forEach { history.recordWarning(it, signal.timestampMs) }
        habitDetector.process(signal)?.let(history::recordEvent)
        _state.value = EngineState(
            drivingState = driving,
            ux = flags,
            signal = signal,
            activeWarnings = explainer.explainAll(signal.activeWarnings),
            seenWarnings = currentSeen(),
            events = history.events.value,
            refuel = fuelMonitor.onReading(signal.odometerKm, signal.fuelPercent, signal.routeRemainingKm),
        )
    }

    private fun currentSeen(): List<WarningCode> {
        val active = _state.value.signal?.activeWarnings.orEmpty()
        return (active + history.warnings.value.map { it.code }).distinct()
    }
}
