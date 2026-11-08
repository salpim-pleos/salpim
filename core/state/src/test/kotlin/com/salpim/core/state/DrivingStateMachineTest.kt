package com.salpim.core.state

import com.salpim.core.model.DrivingState.DRIVING
import com.salpim.core.model.DrivingState.PARKED
import com.salpim.core.model.DrivingState.STOPPED
import com.salpim.core.model.Gear
import com.salpim.core.model.VehicleSignal
import org.junit.Assert.assertEquals
import org.junit.Test

class DrivingStateMachineTest {
    private fun sig(t: Long, kmh: Float?, gear: Gear = Gear.DRIVE, brake: Boolean? = false) =
        VehicleSignal(timestampMs = t, speedKmh = kmh, gear = gear, parkingBrakeOn = brake)

    @Test
    fun entersDrivingImmediatelyAboveEnterThreshold() {
        val sm = DrivingStateMachine()
        assertEquals(DRIVING, sm.update(sig(0, 6f)))
    }

    @Test
    fun belowEnterThresholdDoesNotDrive() {
        val sm = DrivingStateMachine(initial = STOPPED)
        assertEquals(STOPPED, sm.update(sig(0, 4f)))
    }

    @Test
    fun hysteresisKeepsDrivingBetweenThresholds() {
        val sm = DrivingStateMachine()
        sm.update(sig(0, 30f))
        // 3 km/h: 진입 임계(5) 미만이지만 이탈 임계(1) 초과 → 계속 DRIVING
        for (t in 0L..10_000L step 200) assertEquals(DRIVING, sm.update(sig(1_000 + t, 3f)))
    }

    @Test
    fun leavingDrivingIsDebounced() {
        val sm = DrivingStateMachine(DrivingStateConfig(debounceMs = 1_500))
        sm.update(sig(0, 30f))
        assertEquals(DRIVING, sm.update(sig(1_000, 0f)))
        assertEquals(DRIVING, sm.update(sig(2_000, 0f)))
        assertEquals(DRIVING, sm.update(sig(2_499, 0f)))
        assertEquals(STOPPED, sm.update(sig(2_500, 0f)))
    }

    @Test
    fun flickerResetsDebounce() {
        val sm = DrivingStateMachine(DrivingStateConfig(debounceMs = 1_500))
        sm.update(sig(0, 30f))
        sm.update(sig(1_000, 0f))
        sm.update(sig(2_000, 10f)) // 다시 움직임 → 대기 중이던 STOPPED 취소
        assertEquals(DRIVING, sm.update(sig(2_600, 0f)))
        assertEquals(DRIVING, sm.update(sig(4_000, 0f)))
        assertEquals(STOPPED, sm.update(sig(4_100, 0f)))
    }

    @Test
    fun parkGearOrParkingBrakeMeansParked() {
        val sm = DrivingStateMachine(DrivingStateConfig(debounceMs = 0), initial = STOPPED)
        assertEquals(PARKED, sm.update(sig(0, 0f, gear = Gear.PARK)))
        sm.reset(STOPPED)
        assertEquals(PARKED, sm.update(sig(0, 0f, gear = Gear.NEUTRAL, brake = true)))
    }

    @Test
    fun unknownSpeedFallsBackToSafeSide() {
        val sm = DrivingStateMachine(initial = PARKED)
        assertEquals(DRIVING, sm.update(sig(0, null, gear = Gear.DRIVE, brake = false)))
        sm.reset(PARKED)
        assertEquals(PARKED, sm.update(sig(0, null, gear = Gear.PARK, brake = null)))
    }

    @Test
    fun movingWinsOverParkingBrakeFlag() {
        // 주차 브레이크 신호가 잘못 와도 속도가 있으면 제한 UI를 유지한다
        val sm = DrivingStateMachine()
        assertEquals(DRIVING, sm.update(sig(0, 20f, brake = true)))
    }
}
