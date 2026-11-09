package com.salpim.core.signal.pleos

import com.salpim.core.model.Gear
import com.salpim.core.model.VehicleSignal
import com.salpim.core.model.WarningCode

/** SDK 콜백으로 모은 최신 값. 리스너마다 따로 갱신되므로 불변 객체를 통째로 바꾼다 */
internal data class PleosReadings(
    val gear: Gear = Gear.UNKNOWN,
    val odometerKm: Double? = null,
    val fuelPercent: Double? = null,
    val oil: Map<OilKind, WarningCode?> = emptyMap(),
    val flags: Map<FlagKind, WarningCode?> = emptyMap(),
) {
    /** 속도·주차 브레이크는 Vehicle SDK에 없다 (S3) — null(모름)로 두면 상태머신이 기어로 판정한다 */
    fun toSignal(timestampMs: Long) = VehicleSignal(
        timestampMs = timestampMs,
        gear = gear,
        odometerKm = odometerKm,
        fuelPercent = fuelPercent,
        activeWarnings = (oil.values + flags.values).filterNotNull().toSet(),
    )
}
