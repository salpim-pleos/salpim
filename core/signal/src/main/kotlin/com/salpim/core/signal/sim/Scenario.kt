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
        var s = initial
        var t = initial.timestampMs
        val out = mutableListOf<VehicleSignal>()
        for (op in ops) when (op) {
            is Op.Set -> s = op.f(s)
            is Op.Hold -> repeat(steps(op.ms, tickMs)) {
                t += tickMs
                out += s.copy(timestampMs = t)
            }
            is Op.Ramp -> {
                val n = steps(op.ms, tickMs)
                val from = s.speedKmh ?: 0f
                for (i in 1..n) {
                    t += tickMs
                    s = s.copy(speedKmh = from + (op.toKmh - from) * i / n)
                    out += s.copy(timestampMs = t)
                }
            }
        }
        return out
    }

    private fun steps(ms: Long, tickMs: Long) = (ms / tickMs).toInt().coerceAtLeast(1)
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
