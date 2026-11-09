package com.salpim.core.signal.pleos

import ai.pleos.playground.vehicle.constant.control.OilLevelState
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import com.salpim.core.model.Gear
import com.salpim.core.model.WarningCode
import com.salpim.core.model.WarningCodes

/** 오일 계열 신호 종류 (Safety: 엔진오일·브레이크액·변속기오일 레벨, `OilLevelState`) */
internal enum class OilKind { ENGINE, BRAKE_FLUID, TRANSMISSION }

/** Boolean 경고 신호 종류 (Safety) */
internal enum class FlagKind { POWER_DOWN, WASHER_FLUID, SEAT_BELT, AIRBAG_DEPLOYED }

/**
 * Pleos SDK 값 → 앱 내부 모델. SDK 타입은 이 패키지 밖으로 나가지 않는다 (AGENTS.md 2절).
 * 근거: Vehicle 2.3.2 AAR의 실제 시그니처 (docs/spikes/S2.md, S3.md).
 */
internal object PleosMapping {
    /** LOW → 점검 권장, CRITICALLY_LOW → 즉시 정차. NORMAL/HIGH/ERROR는 경고 없음 (HIGH·ERROR는 TODO(spike-S2): 의미 확인 후 다룰지 결정) */
    fun oilWarning(kind: OilKind, state: OilLevelState?): WarningCode? = when (state) {
        OilLevelState.LOW -> lowCode(kind)
        OilLevelState.CRITICALLY_LOW -> criticalCode(kind)
        else -> null
    }

    fun flagWarning(kind: FlagKind, on: Boolean?): WarningCode? = if (on == true) flagCode(kind) else null

    fun gear(value: VehicleGear?): Gear = when (value) {
        VehicleGear.GEAR_PARK -> Gear.PARK
        VehicleGear.GEAR_REVERSE -> Gear.REVERSE
        VehicleGear.GEAR_NEUTRAL -> Gear.NEUTRAL
        null, VehicleGear.GEAR_UNKNOWN -> Gear.UNKNOWN
        else -> Gear.DRIVE // GEAR_DRIVE, GEAR_1..9 (수동 단수 포함)
    }

    private fun lowCode(kind: OilKind) = when (kind) {
        OilKind.ENGINE -> WarningCodes.ENGINE_OIL_LOW
        OilKind.BRAKE_FLUID -> WarningCodes.BRAKE_FLUID_LOW
        OilKind.TRANSMISSION -> WarningCodes.TRANSMISSION_OIL_LOW
    }

    private fun criticalCode(kind: OilKind) = when (kind) {
        OilKind.ENGINE -> WarningCodes.ENGINE_OIL_CRITICAL
        OilKind.BRAKE_FLUID -> WarningCodes.BRAKE_FLUID_CRITICAL
        OilKind.TRANSMISSION -> WarningCodes.TRANSMISSION_OIL_CRITICAL
    }

    private fun flagCode(kind: FlagKind) = when (kind) {
        FlagKind.POWER_DOWN -> WarningCodes.POWER_DOWN
        FlagKind.WASHER_FLUID -> WarningCodes.WASHER_FLUID_LOW
        FlagKind.SEAT_BELT -> WarningCodes.SEAT_BELT_UNBUCKLED
        FlagKind.AIRBAG_DEPLOYED -> WarningCodes.AIRBAG_DEPLOYED
    }
}
