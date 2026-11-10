package com.salpim.app

import android.content.Context
import android.content.pm.PackageManager
import com.salpim.core.data.HistoryRepository
import com.salpim.core.data.InMemoryHistoryRepository
import com.salpim.core.model.NavigationLauncher
import com.salpim.core.model.ServiceDestination
import com.salpim.core.signal.SignalSource
import com.salpim.core.signal.navi.NaviHelperNavigationLauncher
import com.salpim.core.signal.pleos.PleosSignalSource
import com.salpim.core.signal.sim.SimulatedNavigationLauncher
import com.salpim.core.signal.sim.SimulatedSignalSource
import com.salpim.core.state.DrivingStateConfig
import com.salpim.core.ux.StaticUxRestrictionsSource
import com.salpim.core.ux.UxRestrictionsSource
import com.salpim.feature.coach.HabitDetector
import com.salpim.feature.fuel.EfficiencyTracker
import com.salpim.feature.fuel.FuelMonitor
import com.salpim.feature.service.FavoriteShopStore
import com.salpim.feature.service.NearbyPlaceRepository
import com.salpim.feature.service.SampleNearbyPlaceRepository
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

    private val isAutomotive = appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)

    /** 빌드 설정 salpim.signalSource: auto(자동차 기기면 Pleos SDK) | pleos | sim */
    private val usePleos = when (BuildConfig.SIGNAL_SOURCE) {
        "pleos" -> true
        "sim" -> false
        else -> isAutomotive
    }

    /** 시뮬레이션 신호원. 활성이면 화면에 "시뮬레이션 신호"를 항상 표시한다 */
    val simulated: SimulatedSignalSource? = if (usePleos) null else SimulatedSignalSource(scope)

    @Suppress("ForbiddenMethodCall") // 앱 진입점: 실제 시계를 주입하는 곳
    private val signalSource: SignalSource = simulated ?: PleosSignalSource(appContext, scope, clock = System::currentTimeMillis)

    val uxSource: UxRestrictionsSource = UxRestrictionsSource.create(appContext)

    /** 비자동차 기기에서만 존재. e2e 테스트에서 시스템 제한을 흉내 낼 때 사용 */
    val staticUx: StaticUxRestrictionsSource? = uxSource as? StaticUxRestrictionsSource

    val history: HistoryRepository = InMemoryHistoryRepository()
    val explainer: WarningExplainer = WarningExplainer.fromAssets(appContext)

    val favoriteShop = FavoriteShopStore(
        // 시뮬레이션 시연용 단골 정비소 (샘플 목록의 블루핸즈 광희점과 같은 곳)
        initial = if (simulated != null) {
            ServiceDestination(
                "블루핸즈 광희점",
                37.56435,
                126.99817,
                "서울 중구 마른내로 139",
            )
        } else {
            null
        },
    )

    /** 시뮬레이션이면 시연용 내비(길 안내가 "시작된" 흐름만), 아니면 NaviHelper */
    private val navigation: NavigationLauncher = if (simulated != null) {
        SimulatedNavigationLauncher()
    } else {
        NaviHelperNavigationLauncher(appContext).also { if (isAutomotive) it.connect() }
    }
    val serviceNavigator = ServiceNavigator(navigation, favoriteShop)

    /** 블루핸즈·주유소 목록. 현재 서울시청 기준 샘플 (TODO(partner-data) — feature/service/NearbyPlaces.kt) */
    val places: NearbyPlaceRepository = SampleNearbyPlaceRepository.fromAssets(appContext)

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
