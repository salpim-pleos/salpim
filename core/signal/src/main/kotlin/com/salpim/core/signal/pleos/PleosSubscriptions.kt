package com.salpim.core.signal.pleos

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.api.model.capability.AccessType
import ai.pleos.playground.vehicle.constant.control.OilLevelState
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.listener.DistanceDrivenListener
import ai.pleos.playground.vehicle.listener.FuelPercentListener
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import ai.pleos.playground.vehicle.listener.LowWasherFluidWaringListener
import ai.pleos.playground.vehicle.listener.OilLevelStateListener
import ai.pleos.playground.vehicle.listener.PowerDownWarningIndicatorListener
import ai.pleos.playground.vehicle.listener.SeatAirbagDeployedListener
import ai.pleos.playground.vehicle.listener.SeatBeltNotBuckledWarningListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Vehicle SDK 리스너 등록·해제를 한곳에 모은다. 모든 등록은 [unregisterAll]로 되돌린다.
 * 권한: Safety → CAR_SAFETY, Gear·Odometer → CAR_DRIVE, Fuel → CAR_ENERGY (AAR의 @RequiresPermission으로 확인).
 */
internal class PleosSubscriptions(private val vehicle: Vehicle, private val readings: MutableStateFlow<PleosReadings>) {
    private val undo = mutableListOf<() -> Unit>()

    fun registerAll() {
        registerGearAndDistance()
        registerOil()
        registerFlags()
        registerSeatAreas()
    }

    fun unregisterAll() {
        undo.asReversed().forEach { runCatching(it) }
        undo.clear()
    }

    private fun registerGearAndDistance() {
        val gear = vehicle.getGear()
        val gearListener = object : GearSelectionListener {
            override fun onSelected(value: VehicleGear?) = readings.update { it.copy(gear = PleosMapping.gear(value)) }
            override fun onFailed(e: Exception) = logFailure("gear", e)
        }
        gear.registerGearSelection(gearListener)
        undo += { gear.unregisterGearSelection(gearListener) }

        val odometer = vehicle.getOdometer()
        // TODO(spike-S3): 단위 확인 (DistanceDisplayUnit) — 에뮬레이터에서 km인지 검증
        val distance = object : DistanceDrivenListener {
            override fun onDistanceDrivenUpdated(value: Float?) = readings.update {
                it.copy(
                    odometerKm = value?.toDouble(),
                )
            }
            override fun onFailed(e: Exception) = logFailure("distance", e)
        }
        odometer.registerDistanceDriven(distance)
        undo += { odometer.unregisterDistanceDriven(distance) }

        val fuel = vehicle.getFuel()
        val fuelListener = object : FuelPercentListener {
            override fun onFuelPercentUpdated(value: Int?) = readings.update {
                it.copy(
                    fuelPercent = value?.toDouble(),
                )
            }
            override fun onFailed(e: Exception) = logFailure("fuel", e)
        }
        fuel.registerFuelPercent(fuelListener)
        undo += { fuel.unregisterFuelPercent(fuelListener) }
    }

    private fun registerOil() {
        val safety = vehicle.getSafety()
        OilKind.entries.forEach { kind ->
            val l = object : OilLevelStateListener {
                override fun onOilLevelUpdated(state: OilLevelState?) =
                    readings.update { it.copy(oil = it.oil + (kind to PleosMapping.oilWarning(kind, state))) }
                override fun onFailed(e: Exception) = logFailure("oil-$kind", e)
            }
            when (kind) {
                OilKind.ENGINE -> safety.registerEngineOilLevelState(
                    l,
                ).also { undo += { safety.unregisterEngineOilLevelState(l) } }
                OilKind.BRAKE_FLUID -> safety.registerBrakeFluidLevelState(
                    l,
                ).also { undo += { safety.unregisterBrakeFluidLevelState(l) } }
                OilKind.TRANSMISSION ->
                    safety.registerTransmissionOilLevelState(
                        l,
                    ).also { undo += { safety.unregisterTransmissionOilLevelState(l) } }
            }
        }
    }

    private fun registerFlags() {
        val safety = vehicle.getSafety()
        val powerDown = object : PowerDownWarningIndicatorListener {
            override fun onPowerDownWarningIndicatorUpdated(on: Boolean?) = setFlag(FlagKind.POWER_DOWN, on)
            override fun onFailed(e: Exception) = logFailure("power-down", e)
        }
        safety.registerPowerDownWarningIndicator(powerDown)
        undo += { safety.unregisterPowerDownWarningIndicator(powerDown) }

        val washer = object : LowWasherFluidWaringListener {
            override fun onLowWasherFluidWaringUpdated(on: Boolean?) = setFlag(FlagKind.WASHER_FLUID, on)
            override fun onFailed(e: Exception) = logFailure("washer", e)
        }
        safety.registerLowWasherFluidWaringState(washer)
        undo += { safety.unregisterLowWasherFluidWaringState(washer) }
    }

    /** 좌석별 신호는 차종이 지원하는 좌석만 등록한다 (capability 확인) */
    private fun registerSeatAreas() {
        val safety = vehicle.getSafety()
        safety.checkSeatNotBuckledWarningCapabilities(
            { config, areas -> if (config.accessType.readable()) areas.forEach(::registerSeatBelt) },
            { e -> logFailure("seat-belt-capability", e) },
        )
        safety.checkAirbagDeployedCapabilities(
            { config, areas -> if (config.accessType.readable()) areas.forEach(::registerAirbag) },
            { e -> logFailure("airbag-capability", e) },
        )
    }

    private fun registerSeatBelt(area: SeatArea) {
        val safety = vehicle.getSafety()
        val l = object : SeatBeltNotBuckledWarningListener {
            override fun onSeatBeltNotBuckledWarningUpdated(on: Boolean?) = setFlag(FlagKind.SEAT_BELT, on)
            override fun onFailed(e: Exception) = logFailure("seat-belt", e)
        }
        safety.registerSeatBeltNotBuckledWarning(area, l)
        undo += { safety.unregisterSeatBeltNotBuckledWarning(area, l) }
    }

    private fun registerAirbag(area: SeatArea) {
        val safety = vehicle.getSafety()
        val l = object : SeatAirbagDeployedListener {
            override fun onSeatAirbagDeployUpdated(on: Boolean?) = setFlag(FlagKind.AIRBAG_DEPLOYED, on)
            override fun onFailed(e: Exception) = logFailure("airbag", e)
        }
        safety.registerSeatAirbagDeployed(area, l)
        undo += { safety.unregisterSeatAirbagDeployed(area, l) }
    }

    /** 여러 좌석 중 하나라도 켜지면 경고. 단순화: 마지막 값으로 덮어쓴다 — TODO(spike-S2): 좌석별로 합칠지 에뮬레이터에서 확인 */
    private fun setFlag(kind: FlagKind, on: Boolean?) = readings.update {
        it.copy(
            flags = it.flags + (kind to PleosMapping.flagWarning(kind, on)),
        )
    }

    private fun AccessType.readable() = this == AccessType.READ || this == AccessType.READ_WRITE

    private fun logFailure(what: String, e: Exception) {
        Log.w(TAG, "Vehicle SDK $what failed (차종 미지원일 수 있음)", e)
    }

    private companion object {
        const val TAG = "SalpimPleos"
    }
}
