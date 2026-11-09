package com.salpim.core.signal.sim

import com.salpim.core.model.Gear
import com.salpim.core.model.VehicleSignal
import com.salpim.core.model.WarningCodes

/** 시연/테스트용 기본 시나리오. id는 e2e 테스트 태그(scenario_<id>)로도 쓰인다. */
object Scenarios {
    /** 시뮬레이션 차량의 초기 누적 주행거리와 연료 [설계] */
    private const val START_ODOMETER_KM = 48_210.0
    private const val START_FUEL_PERCENT = 60.0

    /** 주차 상태 초기값 */
    fun parked(timestampMs: Long = 0) = VehicleSignal(
        timestampMs = timestampMs,
        speedKmh = 0f,
        gear = Gear.PARK,
        parkingBrakeOn = true,
        steeringAngleDeg = 0f,
        odometerKm = START_ODOMETER_KM,
        fuelPercent = START_FUEL_PERCENT,
    )

    /** 완만한 출발 가속도. 급가속 임계(9 km/h/s) 미만 */
    private const val DEPARTURE_ACCEL_KMH_PER_SEC = 6f
    private const val DEPARTURE_PAUSE_MS = 600L
    private const val MS_PER_SECOND = 1000f

    private fun ScenarioBuilder.departure(kmh: Float) {
        set { copy(gear = Gear.DRIVE, parkingBrakeOn = false) }
        hold(DEPARTURE_PAUSE_MS)
        ramp(kmh, (kmh * MS_PER_SECOND / DEPARTURE_ACCEL_KMH_PER_SEC).toLong())
    }

    val TIRE_WARNING = scenario("tire_warning", "주행 중 타이어 공기압 경고") {
        departure(60f)
        hold(3_000)
        set { copy(activeWarnings = activeWarnings + WarningCodes.TIRE_PRESSURE_LOW) }
        hold(8_000)
    }

    val HARD_BRAKE_SEQUENCE = scenario("hard_brake_sequence", "급가속 · 급제동") {
        departure(30f)
        hold(1_000)
        ramp(70f, 3_000) // 약 13 km/h/s → 급가속
        hold(2_000)
        ramp(20f, 2_000) // 25 km/h/s → 급제동
        hold(2_000)
        ramp(0f, 4_000)
        hold(1_000)
    }

    val PARKING_AFTER_WARNING = scenario("parking_after_warning", "경고 후 정차 · 주차") {
        departure(50f)
        set { copy(activeWarnings = activeWarnings + WarningCodes.TIRE_PRESSURE_LOW) }
        hold(4_000)
        ramp(0f, 8_000) // 6.25 km/h/s, 급제동 임계 미만
        hold(2_000)
        set { copy(gear = Gear.PARK, parkingBrakeOn = true) }
        hold(3_000)
    }

    /** 연료 18%로 220km 떨어진 목적지를 향해 출발 → 주유 예측이 "지금 주유"를 판단 */
    val LOW_FUEL_TRIP = scenario("low_fuel_trip", "연료가 부족한 채로 장거리 출발") {
        set { copy(fuelPercent = 18.0, routeRemainingKm = 220.0) }
        departure(90f)
        hold(10_000)
    }

    val all: List<Scenario> = listOf(TIRE_WARNING, HARD_BRAKE_SEQUENCE, PARKING_AFTER_WARNING, LOW_FUEL_TRIP)
}
