package com.salpim.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.salpim.core.model.DrivingEvent
import com.salpim.core.model.DrivingState
import com.salpim.core.model.Explanation
import com.salpim.core.model.UxPolicy
import com.salpim.core.model.UxRestrictionFlags
import com.salpim.core.model.WarningCode
import com.salpim.core.signal.sim.Scenarios
import com.salpim.feature.coach.CoachReport
import com.salpim.feature.coach.label
import com.salpim.feature.glance.GlanceItem
import com.salpim.feature.glance.GlanceModel
import com.salpim.feature.glance.GlanceTone
import com.salpim.feature.service.NearbyPlace
import com.salpim.feature.service.PlaceCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ScenarioInfo(val id: String, val title: String)

data class MainUiState(
    val drivingState: DrivingState = DrivingState.PARKED,
    val policy: UxPolicy = UxPolicy.resolve(DrivingState.PARKED, UxRestrictionFlags.NONE),
    val simulated: Boolean = false,
    /** DRIVING 중 한 줄 배너로 보여 줄 가장 긴급한 경고 */
    val banner: Explanation? = null,
    /** DRIVING 중 표시 항목. 배너와 합쳐 최대 3개 */
    val glance: List<GlanceItem> = emptyList(),
    val warningList: List<Explanation> = emptyList(),
    val selected: Explanation? = null,
    val report: CoachReport = CoachReport.from(emptyList()),
    val navMessage: String? = null,
    /** "가까운 블루핸즈 찾기" 결과. null이면 아직 안 찾음 */
    val serviceCenters: List<NearbyPlace>? = null,
    val serviceCentersMock: Boolean = false,
    val scenarios: List<ScenarioInfo> = emptyList(),
    val playingScenario: String? = null,
)

/** 사용자 조작으로 바뀌는 화면 입력 */
private data class UserInputs(
    val selectedCode: WarningCode?,
    val navMessage: String?,
    val playingScenario: String?,
    val serviceCenters: List<NearbyPlace>?,
)

class MainViewModel(private val c: AppContainer) : ViewModel() {
    private val selectedCode = MutableStateFlow<WarningCode?>(null)
    private val navMessage = MutableStateFlow<String?>(null)
    private val centers = MutableStateFlow<List<NearbyPlace>?>(null)
    private val scenarios = if (c.simulated != null) {
        Scenarios.all.map {
            ScenarioInfo(
                it.id,
                it.title,
            )
        }
    } else {
        emptyList()
    }

    private val userInputs = combine(
        selectedCode,
        navMessage,
        c.simulated?.playing ?: flowOf(null),
        centers,
        ::UserInputs,
    )

    val ui: StateFlow<MainUiState> = combine(c.engine.state, userInputs, ::buildState)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            MainUiState(simulated = c.simulated != null),
        )

    fun selectWarning(code: WarningCode) {
        selectedCode.value = code
    }

    /** 정차 화면에서만 호출된다 (주행 중에는 버튼이 없음) */
    fun findNearby(category: PlaceCategory) {
        viewModelScope.launch { centers.value = c.places.nearby(category) }
    }

    fun routeTo(center: NearbyPlace) {
        viewModelScope.launch { navMessage.value = c.serviceNavigator.navigateTo(center.toDestination()) }
    }

    fun playScenario(id: String) {
        val sim = c.simulated ?: return
        Scenarios.all.firstOrNull { it.id == id }?.let(sim::play)
    }

    fun clearHistory() {
        c.engine.clearHistory()
        navMessage.value = null
    }

    private fun buildState(engine: EngineState, inputs: UserInputs): MainUiState {
        val policy = UxPolicy.resolve(engine.drivingState, engine.ux)
        val report = CoachReport.from(engine.events)
        val banner = engine.activeWarnings.firstOrNull()
        val warnings = (engine.activeWarnings.map { it.code } + engine.seenWarnings).distinct().map(
            c.explainer::explain,
        )
        return MainUiState(
            drivingState = engine.drivingState,
            policy = policy,
            simulated = c.simulated != null,
            banner = banner,
            glance = glanceItems(engine, report, policy, hasBanner = banner != null),
            warningList = warnings,
            selected = warnings.firstOrNull { it.code == inputs.selectedCode } ?: warnings.firstOrNull(),
            report = report,
            navMessage = inputs.navMessage,
            serviceCenters = inputs.serviceCenters,
            serviceCentersMock = c.places.isMock,
            scenarios = scenarios,
            playingScenario = inputs.playingScenario,
        )
    }

    /** 배너가 있으면 글랜스는 한 칸 줄여 주행 중 정보 3개를 넘지 않게 한다 */
    private fun glanceItems(
        engine: EngineState,
        report: CoachReport,
        policy: UxPolicy,
        hasBanner: Boolean,
    ): List<GlanceItem> {
        val recent = recentEvent(engine)
        val limit = if (hasBanner) UxPolicy.DRIVING_MAX_ITEMS - 1 else UxPolicy.DRIVING_MAX_ITEMS
        return GlanceModel.build(
            engine.activeWarnings,
            report.score,
            FuelText.summary(engine.refuel),
            policy.maxGlanceItems,
        )
            .filterNot { hasBanner && it.id == GLANCE_WARNING_ID }
            .map { if (recent != null && it.id == GLANCE_SCORE_ID) recentEventItem(recent) else it }
            .take(limit)
    }

    private fun recentEvent(engine: EngineState): DrivingEvent? {
        val nowMs = engine.signal?.timestampMs ?: return null
        return engine.events.lastOrNull()?.takeIf { nowMs - it.timestampMs < RECENT_EVENT_MS }
    }

    private fun recentEventItem(event: DrivingEvent) = GlanceItem(
        GLANCE_SCORE_ID,
        "방금",
        event.type.label(),
        GlanceTone.CAUTION,
    )

    class Factory(private val c: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(c) as T
    }

    private companion object {
        const val RECENT_EVENT_MS = 3_000L
        const val STOP_TIMEOUT_MS = 5_000L
        const val GLANCE_WARNING_ID = "warning"
        const val GLANCE_SCORE_ID = "score"
    }
}
