package com.salpim.core.signal.pleos

import android.content.Context
import com.salpim.core.model.VehicleSignal
import com.salpim.core.signal.SignalSource
import com.salpim.core.signal.SignalSourceKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pleos Vehicle SDK 기반 신호원. **아직 SDK를 호출하지 않는다.**
 *
 * 확인된 패턴 (CLAUDE.md): `vehicle.initialize()` → `vehicle.getXXX()` → 종료 시 `vehicle.release()`,
 * 차종별 `checkXXXCapability` 확인 필수.
 *
 * TODO(spike-S6): SDK 의존성 좌표/클래스 이름 확인 후 구현. 문서에 없는 이름은 추측하지 않는다.
 * TODO(spike-S2): Safety/Tire 경고 → WarningCode 매핑
 * TODO(spike-S3): 속도/기어/주차 브레이크 소스 확정 (DrivingMode, Brake, CarPropertyManager 대조)
 */
class PleosSignalSource(
    @Suppress("unused") private val context: Context,
) : SignalSource {
    override val kind = SignalSourceKind.VEHICLE

    private val _signals = MutableStateFlow(VehicleSignal(timestampMs = System.currentTimeMillis()))
    override val signals: StateFlow<VehicleSignal> = _signals.asStateFlow()

    override fun start() {
        // TODO(spike-S6): vehicle.initialize()
    }

    override fun stop() {
        // TODO(spike-S6): vehicle.release()
    }

    companion object {
        /** SDK 연동 전까지 false. 앱은 false면 SimulatedSignalSource를 쓴다. */
        const val IMPLEMENTED: Boolean = false
    }
}
