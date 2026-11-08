package com.salpim.core.signal

import com.salpim.core.model.VehicleSignal
import kotlinx.coroutines.flow.StateFlow

enum class SignalSourceKind {
    /** 실제 차량 또는 Pleos 에뮬레이터 신호 */
    VEHICLE,

    /** 시나리오 재생. 화면에 SIMULATED 배지를 반드시 표시한다 */
    SIMULATED,
}

/**
 * 차량 신호의 단일 진입점. 나머지 코드는 이 인터페이스와 core/model 타입만 본다.
 * 구현은 주기적으로(예: 200ms) 최신 스냅샷을 내보내야 한다 (DrivingStateMachine 디바운스용).
 */
interface SignalSource {
    val kind: SignalSourceKind
    val signals: StateFlow<VehicleSignal>

    /** Pleos 구현에서는 vehicle.initialize() 시점 */
    fun start()

    /** Pleos 구현에서는 vehicle.release() 시점 */
    fun stop()
}
