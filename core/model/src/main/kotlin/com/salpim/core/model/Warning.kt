package com.salpim.core.model

/**
 * 앱 내부 경고 코드. SDK 값을 그대로 노출하지 않는다.
 * 서드파티 앱이 받을 수 있는 Safety 공개 API(`CAR_SAFETY`) 기준 [확인됨, S2]:
 * 오일 3종 레벨(`OilLevelState`), 파워다운, 워셔액, 안전벨트, 에어백 전개.
 * 계기판 경고등(telltale)·타이어 공기압은 signature 권한이라 다루지 않는다.
 * SDK 값 → 이 코드 매핑은 core/signal 에서 한다 (TODO(spike-S2): 실제 값 확인 후 PleosSignalSource).
 */
@JvmInline
value class WarningCode(val value: String) {
    override fun toString(): String = value
}

object WarningCodes {
    /** `getEngineOilLevelState` = LOW */
    val ENGINE_OIL_LOW = WarningCode("ENGINE_OIL_LOW")

    /** `getEngineOilLevelState` = CRITICALLY_LOW */
    val ENGINE_OIL_CRITICAL = WarningCode("ENGINE_OIL_CRITICAL")
    val BRAKE_FLUID_LOW = WarningCode("BRAKE_FLUID_LOW")
    val BRAKE_FLUID_CRITICAL = WarningCode("BRAKE_FLUID_CRITICAL")
    val TRANSMISSION_OIL_LOW = WarningCode("TRANSMISSION_OIL_LOW")
    val TRANSMISSION_OIL_CRITICAL = WarningCode("TRANSMISSION_OIL_CRITICAL")

    /** `hasPowerDownWarningIndicator` — 문서: 엔진에 문제가 있음 */
    val POWER_DOWN = WarningCode("POWER_DOWN")

    /** `hasLowWasherFluidWarning` */
    val WASHER_FLUID_LOW = WarningCode("WASHER_FLUID_LOW")

    /** `hasSeatBeltNotBuckledWarning` */
    val SEAT_BELT_UNBUCKLED = WarningCode("SEAT_BELT_UNBUCKLED")

    /** `hasSeatAirbagBeenDeployed` */
    val AIRBAG_DEPLOYED = WarningCode("AIRBAG_DEPLOYED")

    val ALL: List<WarningCode> = listOf(
        ENGINE_OIL_LOW, ENGINE_OIL_CRITICAL, BRAKE_FLUID_LOW, BRAKE_FLUID_CRITICAL,
        TRANSMISSION_OIL_LOW, TRANSMISSION_OIL_CRITICAL, POWER_DOWN, WASHER_FLUID_LOW,
        SEAT_BELT_UNBUCKLED, AIRBAG_DEPLOYED,
    )
}

/** 차 그림에서 칠할 위치. 차를 모르는 사람도 "어디가 문제인지" 알 수 있게 한다 */
enum class CarZone {
    ENGINE,
    BRAKE,
    TRANSMISSION,
    WASHER,
    SEAT_BELT,
    AIRBAG,
}

enum class Urgency {
    /** 즉시 안전한 곳에 정차 */
    IMMEDIATE_STOP,

    /** 가까운 시일 내 점검 */
    SERVICE_SOON,
    INFO,
}

/** 경고 해설. 단정하지 않고 "점검 권장" 톤을 유지한다 (AGENTS.md 안전/UX 규칙 7). */
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
    /** 부위(아이콘·부품 타일). 모르면 null */
    val zone: CarZone? = null,
    /** 부품의 쉬운 설명. 예: "엔진이 잘 돌도록 돕는 기름" */
    val plainMeaning: String? = null,
)
