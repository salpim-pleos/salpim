package com.salpim.core.signal.sim

import com.salpim.core.model.VehicleSignal

/**
 * 신호 재생 시나리오. [frames]는 순수 함수라 단위 테스트에서 시간 없이 그대로 쓸 수 있다.
 */
class Scenario internal constructor(
    val id: String,
    val title: String,
    private val ops: List<Op>,
) {
    internal sealed interface Op {
        class Set(val f: VehicleSignal.() -> VehicleSignal) : Op
        class Hold(val ms: Long) : Op
        class Ramp(val toKmh: Float, val ms: Long) : Op
    }

    /** initial 상태에서 시작해 tickMs 간격 프레임 목록을 만든다. timestamp는 initial 기준으로 증가. */
    fun frames(tickMs: Long, initial: VehicleSignal): List<VehicleSignal> {
        require(tickMs > 0)
        val writer = FrameWriter(tickMs, initial.timestampMs)
        var state = initial
        ops.forEach { state = it.applyTo(state, writer) }
        return writer.frames
    }

    private fun Op.applyTo(state: VehicleSignal, writer: FrameWriter): VehicleSignal = when (this) {
        is Op.Set -> f(state).also { writer.overrideChanged(before = state, after = it) }
        is Op.Hold -> state.also { repeat(writer.stepsFor(ms)) { writer.emit(state) } }
        is Op.Ramp -> rampFrom(state, writer)
    }

    private fun Op.Ramp.rampFrom(start: VehicleSignal, writer: FrameWriter): VehicleSignal {
        val steps = writer.stepsFor(ms)
        val fromKmh = start.speedKmh ?: 0f
        var state = start
        for (step in 1..steps) {
            state = state.copy(speedKmh = fromKmh + (toKmh - fromKmh) * step / steps)
            writer.emit(state)
        }
        return state
    }

    /** tickMs 간격으로 timestamp를 매기고, 주행거리·연료를 진행시켜 프레임을 쌓는다 */
    private class FrameWriter(private val tickMs: Long, startMs: Long) {
        private var timeMs = startMs
        private var odometerKm: Double? = null
        private var fuelPercent: Double? = null
        private var routeRemainingKm: Double? = null
        val frames = mutableListOf<VehicleSignal>()

        fun stepsFor(durationMs: Long) = (durationMs / tickMs).toInt().coerceAtLeast(1)

        /** 시나리오가 set으로 바꾼 값이 없으면 직전 프레임에서 이어서 진행한다 */
        fun emit(state: VehicleSignal) {
            timeMs += tickMs
            val continued = state.copy(
                odometerKm = odometerKm ?: state.odometerKm,
                fuelPercent = fuelPercent ?: state.fuelPercent,
                routeRemainingKm = routeRemainingKm ?: state.routeRemainingKm,
            )
            val advanced = SimulatedVehicleModel.advance(continued, tickMs).copy(timestampMs = timeMs)
            odometerKm = advanced.odometerKm
            fuelPercent = advanced.fuelPercent
            routeRemainingKm = advanced.routeRemainingKm
            frames += advanced
        }

        /** set 단계에서 시나리오가 직접 바꾼 값만 따른다 (나머지는 진행 중인 값 유지) */
        fun overrideChanged(before: VehicleSignal, after: VehicleSignal) {
            if (after.odometerKm != before.odometerKm) odometerKm = after.odometerKm
            if (after.fuelPercent != before.fuelPercent) fuelPercent = after.fuelPercent
            if (after.routeRemainingKm != before.routeRemainingKm) routeRemainingKm = after.routeRemainingKm
        }
    }
}

class ScenarioBuilder internal constructor() {
    internal val ops = mutableListOf<Scenario.Op>()

    /** 즉시 상태 변경 (프레임은 다음 hold/ramp에서 나간다) */
    fun set(f: VehicleSignal.() -> VehicleSignal) {
        ops += Scenario.Op.Set(f)
    }

    fun hold(ms: Long) {
        ops += Scenario.Op.Hold(ms)
    }

    /** 현재 속도에서 toKmh까지 ms 동안 선형 변화 */
    fun ramp(toKmh: Float, ms: Long) {
        ops += Scenario.Op.Ramp(toKmh, ms)
    }
}

fun scenario(id: String, title: String, block: ScenarioBuilder.() -> Unit): Scenario =
    Scenario(id, title, ScenarioBuilder().apply(block).ops.toList())
