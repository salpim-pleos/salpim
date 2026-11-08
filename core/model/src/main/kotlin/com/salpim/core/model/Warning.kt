package com.salpim.core.model

/**
 * 앱 내부 경고 코드. [설계]
 * Safety/Tire API가 실제로 어떤 값을 주는지는 spike S2에서 확인한 뒤
 * core/signal 에서 이 코드로 매핑한다. SDK 값을 그대로 노출하지 않는다.
 */
@JvmInline
value class WarningCode(val value: String) {
    override fun toString(): String = value
}

object WarningCodes {
    val TIRE_PRESSURE_LOW = WarningCode("TIRE_PRESSURE_LOW")
    val BRAKE_SYSTEM = WarningCode("BRAKE_SYSTEM")
    val ENGINE_OIL_PRESSURE = WarningCode("ENGINE_OIL_PRESSURE")
    val COOLANT_TEMP_HIGH = WarningCode("COOLANT_TEMP_HIGH")
    val CHARGING_SYSTEM = WarningCode("CHARGING_SYSTEM")
    val AIRBAG = WarningCode("AIRBAG")
    val LOW_FUEL = WarningCode("LOW_FUEL")
    val EV_BATTERY_LOW = WarningCode("EV_BATTERY_LOW")

    /** 연료/배터리 부족처럼 글랜스의 "에너지" 항목으로 보여 줄 코드 */
    val ENERGY: Set<WarningCode> = setOf(LOW_FUEL, EV_BATTERY_LOW)
}

enum class Urgency {
    /** 즉시 안전한 곳에 정차 */
    IMMEDIATE_STOP,

    /** 가까운 시일 내 점검 */
    SERVICE_SOON,
    INFO,
}

/** 경고 해설. 단정하지 않고 "점검 권장" 톤을 유지한다 (CLAUDE.md 안전/UX 규칙 7). */
data class Explanation(
    val code: WarningCode,
    val title: String,
    /** 주행 중 표시할 한 줄 */
    val oneLine: String,
    val detail: String,
    val causes: List<String>,
    val actions: List<String>,
    val urgency: Urgency,
    val needsService: Boolean,
    /** 지식 베이스에 있는 코드인지. false면 폴백 설명 */
    val known: Boolean,
)
