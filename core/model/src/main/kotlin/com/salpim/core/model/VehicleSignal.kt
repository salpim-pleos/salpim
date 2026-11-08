package com.salpim.core.model

enum class Gear { PARK, REVERSE, NEUTRAL, DRIVE, UNKNOWN }

/**
 * 한 시점의 차량 신호 스냅샷.
 *
 * 값이 null이면 "모름"이다 (차종/권한/capability에 따라 지원하지 않을 수 있음).
 * 실제 소스 매핑은 spike S2(경고), S3(속도/기어/주차브레이크)에서 확정한다.
 */
data class VehicleSignal(
    val timestampMs: Long,
    val speedKmh: Float? = null,
    val gear: Gear = Gear.UNKNOWN,
    val parkingBrakeOn: Boolean? = null,
    val steeringAngleDeg: Float? = null,
    val activeWarnings: Set<WarningCode> = emptySet(),
)
