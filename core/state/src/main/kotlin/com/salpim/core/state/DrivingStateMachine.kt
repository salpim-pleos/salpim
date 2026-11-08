package com.salpim.core.state

import com.salpim.core.model.DrivingState
import com.salpim.core.model.Gear
import com.salpim.core.model.VehicleSignal

/**
 * @param debounceMs DRIVING을 벗어나는 전환, PARKED<->STOPPED 전환에 적용하는 디바운스
 * @param enterDrivingDebounceMs DRIVING으로 들어가는 전환의 디바운스. [설계] 기본 0:
 *   움직이기 시작했는데 제한 없는 UI가 남아 있으면 안 되므로 진입은 즉시, 진입 떨림은 히스테리시스로 막는다.
 * @param driveEnterKmh 이 속도 이상이면 주행으로 본다
 * @param driveExitKmh DRIVING 상태에서는 이 속도 이하로 떨어져야 정지로 본다 (히스테리시스)
 */
data class DrivingStateConfig(
    val debounceMs: Long = 1_500,
    val enterDrivingDebounceMs: Long = 0,
    val driveEnterKmh: Float = 5f,
    val driveExitKmh: Float = 1f,
) {
    init {
        require(driveExitKmh <= driveEnterKmh) { "driveExitKmh must be <= driveEnterKmh" }
    }
}

/**
 * VehicleSignal 스트림 → DrivingState. 시간은 신호의 timestamp만 쓴다 (테스트 결정성).
 * 디바운스는 신호가 들어올 때 평가되므로 SignalSource는 주기적으로 스냅샷을 내보내야 한다.
 */
class DrivingStateMachine(
    private val config: DrivingStateConfig = DrivingStateConfig(),
    initial: DrivingState = DrivingState.PARKED,
) {
    var state: DrivingState = initial
        private set

    private var pending: DrivingState? = null
    private var pendingSinceMs: Long = 0

    fun update(signal: VehicleSignal): DrivingState {
        val target = classify(signal)
        if (target == state) {
            pending = null
            return state
        }
        if (target != pending) {
            pending = target
            pendingSinceMs = signal.timestampMs
        }
        val required = if (target == DrivingState.DRIVING) config.enterDrivingDebounceMs else config.debounceMs
        if (signal.timestampMs - pendingSinceMs >= required) {
            state = target
            pending = null
        }
        return state
    }

    fun reset(to: DrivingState = DrivingState.PARKED) {
        state = to
        pending = null
    }

    internal fun classify(signal: VehicleSignal): DrivingState {
        val parked = signal.gear == Gear.PARK || signal.parkingBrakeOn == true
        val speed = signal.speedKmh
        val moving = if (speed == null) {
            // [설계] 속도를 모르면 안전 쪽(제한)으로: 주행 기어 + 주차 브레이크 해제면 주행으로 본다
            !parked && (signal.gear == Gear.DRIVE || signal.gear == Gear.REVERSE)
        } else {
            val threshold = if (state == DrivingState.DRIVING) config.driveExitKmh else config.driveEnterKmh
            if (state == DrivingState.DRIVING) speed > threshold else speed >= threshold
        }
        return when {
            moving -> DrivingState.DRIVING
            parked -> DrivingState.PARKED
            else -> DrivingState.STOPPED
        }
    }
}
