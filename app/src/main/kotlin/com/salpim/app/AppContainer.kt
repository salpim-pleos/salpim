package com.salpim.app

import android.content.Context
import com.salpim.core.data.HistoryRepository
import com.salpim.core.data.InMemoryHistoryRepository
import com.salpim.core.model.ServiceDestination
import com.salpim.core.signal.SignalSource
import com.salpim.core.signal.navi.NaviHelperNavigationLauncher
import com.salpim.core.signal.pleos.PleosSignalSource
import com.salpim.core.signal.sim.SimulatedSignalSource
import com.salpim.core.state.DrivingStateConfig
import com.salpim.core.ux.StaticUxRestrictionsSource
import com.salpim.core.ux.UxRestrictionsSource
import com.salpim.feature.coach.HabitDetector
import com.salpim.feature.fuel.EfficiencyTracker
import com.salpim.feature.fuel.FuelMonitor
import com.salpim.feature.service.FavoriteShopStore
import com.salpim.feature.service.MockNearbyPlaceRepository
import com.salpim.feature.service.NearbyPlaceRepository
import com.salpim.feature.service.ServiceNavigator
import com.salpim.feature.voice.SpeechInput
import com.salpim.feature.voice.SpeechOutput
import com.salpim.feature.voice.android.AndroidSpeechInput
import com.salpim.feature.voice.android.AndroidSpeechOutput
import com.salpim.feature.warning.WarningExplainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** 수동 DI. 앱 전체에서 하나. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Pleos SDK 연동 전까지는 항상 시뮬레이션. 활성이면 화면에 SIMULATED 배지를 표시한다. */
    val simulated: SimulatedSignalSource? =
        if (PleosSignalSource.IMPLEMENTED) null else SimulatedSignalSource(scope)

    private val signalSource: SignalSource = simulated ?: PleosSignalSource(appContext)

    val uxSource: UxRestrictionsSource = UxRestrictionsSource.create(appContext)

    /** 비자동차 기기에서만 존재. e2e 테스트에서 시스템 제한을 흉내 낼 때 사용 */
    val staticUx: StaticUxRestrictionsSource? = uxSource as? StaticUxRestrictionsSource

    val history: HistoryRepository = InMemoryHistoryRepository()
    val explainer: WarningExplainer = WarningExplainer.fromAssets(appContext)

    val favoriteShop = FavoriteShopStore(
        // 시뮬레이션 시연용 예시 정비소. 실제 위치 아님
        initial = if (simulated != null) ServiceDestination("예시 정비소", 37.5665, 126.9780) else null,
    )
    val serviceNavigator = ServiceNavigator(NaviHelperNavigationLauncher(appContext), favoriteShop)

    /** 블루핸즈·주유소 목록. 현재 mock (TODO(partner-data) — feature/service/NearbyPlaces.kt) */
    val places: NearbyPlaceRepository = MockNearbyPlaceRepository.fromAssets(appContext)

    /** 음성 입출력. Android 기본 엔진 — TODO(spike-S6): Pleos STT/TTS SDK ON_DEVICE로 교체 검토 */
    val speechInput: SpeechInput = AndroidSpeechInput(appContext)
    val speechOutput: SpeechOutput = AndroidSpeechOutput(appContext)

    val engine = SalpimEngine(
        scope = scope,
        signalSource = signalSource,
        uxSource = uxSource,
        stateConfig = DrivingStateConfig(),
        analyzers = SignalAnalyzers(
            explainer = explainer,
            habitDetector = HabitDetector(),
            fuelMonitor = FuelMonitor(EfficiencyTracker(initialKmPerPercent = demoEfficiencyHistory())),
        ),
        history = history,
    )

    /** 시뮬레이션일 때만 시연용 연비 기록을 미리 채운다 */
    private fun demoEfficiencyHistory(): List<Double> = if (simulated != null) DEMO_EFFICIENCY_HISTORY else emptyList()

    private companion object {
        /**
         * 시뮬레이션 시연용 과거 연비 기록(연료 1%당 km). 실제 차량 기록이 아니다.
         * 분포: 하위 10% ≈ 5.34, 하위 25% = 8.9 → 연료 18%면 약 96~160km (캔버스 "주유소 안내" 시안과 같은 값)
         */
        val DEMO_EFFICIENCY_HISTORY = listOf(4.8, 5.4, 8.8, 9.2, 9.6, 10.0, 10.4, 10.8, 11.2, 11.6)
    }

    /** e2e 테스트용: 신호/상태/기록 초기화 */
    fun resetForTest() {
        simulated?.reset()
        staticUx?.set(com.salpim.core.model.UxRestrictionFlags.NONE)
        engine.reset()
    }
}
