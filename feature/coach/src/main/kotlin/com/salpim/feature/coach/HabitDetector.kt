package com.salpim.feature.coach

import com.salpim.core.model.DrivingEvent
import com.salpim.core.model.DrivingEventType
import com.salpim.core.model.VehicleSignal
import kotlin.math.abs

/**
 * 임계값. 에뮬레이터/시연에서 튜닝한다.
 * 참고: 1 g ≈ 35.3 km/h/s. 10 km/h/s ≈ 0.28 g.
 */
data class HabitConfig(
    val hardBrakeKmhPerSec: Float = 10f,
    val hardAccelKmhPerSec: Float = 9f,
    val sharpTurnDeg: Float = 90f,
    val sharpTurnMinKmh: Float = 40f,
    /** 같은 종류 이벤트 중복 방지 */
    val cooldownMs: Long = 3_000,
    /** 너무 짧은 간격의 샘플은 노이즈로 보고 건너뛴다 */
    val minSampleGapMs: Long = 100,
)

/**
 * 속도 샘플 변화율로 급가속/급제동, 조향각으로 급조향을 검출한다. [설계]
 * Brake API는 페달 상태만 주므로 감속도는 속도에서 계산한다.
 */
class HabitDetector(private val config: HabitConfig = HabitConfig()) {
    private var prev: VehicleSignal? = null
    private val lastAt = mutableMapOf<DrivingEventType, Long>()

    fun process(signal: VehicleSignal): DrivingEvent? {
        val previous = prev
        if (previous != null && signal.timestampMs - previous.timestampMs < config.minSampleGapMs) return null
        prev = signal
        val event = previous?.let { detect(it, signal) } ?: return null
        return event.takeUnless(::inCooldown)?.also { lastAt[it.type] = it.timestampMs }
    }

    fun reset() {
        prev = null
        lastAt.clear()
    }

    private fun detect(previous: VehicleSignal, current: VehicleSignal): DrivingEvent? =
        speedEvent(previous, current, current.timestampMs - previous.timestampMs) ?: turnEvent(current)

    private fun inCooldown(event: DrivingEvent): Boolean =
        lastAt[event.type]?.let { event.timestampMs - it < config.cooldownMs } ?: false

    private fun speedEvent(p: VehicleSignal, s: VehicleSignal, dt: Long): DrivingEvent? {
        val v0 = p.speedKmh ?: return null
        val v1 = s.speedKmh ?: return null
        val rate = (v1 - v0) * MS_PER_SECOND / dt
        return when {
            rate <= -config.hardBrakeKmhPerSec -> DrivingEvent(DrivingEventType.HARD_BRAKE, s.timestampMs, -rate)
            rate >= config.hardAccelKmhPerSec -> DrivingEvent(DrivingEventType.HARD_ACCEL, s.timestampMs, rate)
            else -> null
        }
    }

    private fun turnEvent(s: VehicleSignal): DrivingEvent? {
        val angle = s.steeringAngleDeg ?: return null
        val speed = s.speedKmh ?: return null
        return if (abs(angle) >= config.sharpTurnDeg && speed >= config.sharpTurnMinKmh) {
            DrivingEvent(DrivingEventType.SHARP_TURN, s.timestampMs, angle)
        } else {
            null
        }
    }

    private companion object {
        const val MS_PER_SECOND = 1000f
    }
}

data class CoachReport(
    val score: Int,
    val counts: Map<DrivingEventType, Int>,
    val tips: List<String>,
) {
    companion object {
        const val MAX_SCORE = 100
        private const val MAX_TIPS = 2

        /** 이벤트 1회당 감점 [설계] — 시연에서 튜닝 */
        private val PENALTY = mapOf(
            DrivingEventType.HARD_BRAKE to 10,
            DrivingEventType.HARD_ACCEL to 7,
            DrivingEventType.SHARP_TURN to 5,
        )

        fun from(events: List<DrivingEvent>): CoachReport {
            val counts = DrivingEventType.entries.associateWith { t -> events.count { it.type == t } }
            val penalty = counts.entries.sumOf { (type, count) -> PENALTY.getValue(type) * count }
            val tips = counts.filterValues { it > 0 }
                .entries.sortedByDescending { it.value }
                .take(MAX_TIPS)
                .map { tipFor(it.key) }
                .ifEmpty { listOf("부드럽게 운전하고 있어요. 지금처럼 유지하세요.") }
            return CoachReport((MAX_SCORE - penalty).coerceIn(0, MAX_SCORE), counts, tips)
        }

        private fun tipFor(type: DrivingEventType) = when (type) {
            DrivingEventType.HARD_BRAKE -> "앞차와 거리를 조금 더 두면 급제동을 줄일 수 있어요."
            DrivingEventType.HARD_ACCEL -> "출발할 때 가속 페달을 천천히 밟아 보세요."
            DrivingEventType.SHARP_TURN -> "회전 전에 속도를 충분히 줄여 보세요."
        }
    }
}

fun DrivingEventType.label(): String = when (this) {
    DrivingEventType.HARD_BRAKE -> "급제동"
    DrivingEventType.HARD_ACCEL -> "급가속"
    DrivingEventType.SHARP_TURN -> "급조향"
}
