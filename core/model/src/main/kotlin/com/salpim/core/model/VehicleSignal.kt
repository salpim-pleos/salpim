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
    /** 누적 주행거리. Odometer(`CAR_DRIVE`) — TODO(spike-S6): 메서드 확인 */
    val odometerKm: Double? = null,
    /** 연료 잔량 0..100. Fuel(`CAR_ENERGY`) — TODO(spike-S6): 메서드 확인 */
    val fuelPercent: Double? = null,
    /**
     * 내비 경로의 남은 거리. 차량 신호가 아니라 NaviHelper 경로 정보지만, 같은 주기로 함께 흘려보내려고 여기 둔다 [설계].
     * TODO(spike-S5): NaviHelper에서 남은 거리를 얻는 방법 확인
     */
    val routeRemainingKm: Double? = null,
)
