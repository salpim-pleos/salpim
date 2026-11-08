package com.salpim.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.salpim.core.model.DrivingState
import com.salpim.core.model.Explanation
import com.salpim.core.model.UxPolicy
import com.salpim.core.model.WarningCode
import com.salpim.core.signal.sim.Scenarios
import com.salpim.feature.coach.CoachReport
import com.salpim.feature.coach.label
import com.salpim.feature.glance.GlanceItem
import com.salpim.feature.glance.GlanceModel
import com.salpim.feature.glance.GlanceTone
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
    val policy: UxPolicy = UxPolicy.resolve(DrivingState.PARKED, com.salpim.core.model.UxRestrictionFlags.NONE),
    val simulated: Boolean = false,
    /** DRIVING 중 한 줄 배너로 보여 줄 가장 긴급한 경고 */
    val banner: Explanation? = null,
    /** DRIVING 중 표시 항목. 배너와 합쳐 최대 3개 */
    val glance: List<GlanceItem> = emptyList(),
    val warningList: List<Explanation> = emptyList(),
    val selected: Explanation? = null,
    val report: CoachReport = CoachReport.from(emptyList()),
    val navMessage: String? = null,
    val scenarios: List<ScenarioInfo> = emptyList(),
    val playingScenario: String? = null,
)

class MainViewModel(private val c: AppContainer) : ViewModel() {
    private val selectedCode = MutableStateFlow<WarningCode?>(null)
    private val navMessage = MutableStateFlow<String?>(null)
    private val scenarios = if (c.simulated != null) Scenarios.all.map { ScenarioInfo(it.id, it.title) } else emptyList()

    val ui: StateFlow<MainUiState> = combine(
        c.engine.state,
        selectedCode,
        navMessage,
        c.simulated?.playing ?: flowOf(null),
    ) { engine, selected, nav, playing ->
        val policy = UxPolicy.resolve(engine.drivingState, engine.ux)
        val report = CoachReport.from(engine.events)
        val banner = engine.activeWarnings.firstOrNull { it.code.value !in ENERGY_CODES }
        val now = engine.signal?.timestampMs ?: 0
        val recent = engine.events.lastOrNull()?.takeIf { now - it.timestampMs < RECENT_EVENT_MS }

        val glance = GlanceModel.build(engine.activeWarnings, report.score, policy.maxGlanceItems)
            .filterNot { banner != null && it.id == "warning" }
            .map { if (recent != null && it.id == "score") GlanceItem("score", "방금", recent.type.label(), GlanceTone.CAUTION) else it }
            .take(if (banner != null) UxPolicy.DRIVING_MAX_ITEMS - 1 else UxPolicy.DRIVING_MAX_ITEMS)

        val list = (engine.activeWarnings.map { it.code } + engine.seenWarnings).distinct().map(c.explainer::explain)
        MainUiState(
            drivingState = engine.drivingState,
            policy = policy,
            simulated = c.simulated != null,
            banner = banner,
            glance = glance,
            warningList = list,
            selected = list.firstOrNull { it.code == selected } ?: list.firstOrNull(),
            report = report,
            navMessage = nav,
            scenarios = scenarios,
            playingScenario = playing,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState(simulated = c.simulated != null))

    fun selectWarning(code: WarningCode) {
        selectedCode.value = code
    }

    fun navigateToService() {
        viewModelScope.launch { navMessage.value = c.serviceNavigator.navigateToFavorite() }
    }

    fun playScenario(id: String) {
        val sim = c.simulated ?: return
        Scenarios.all.firstOrNull { it.id == id }?.let(sim::play)
    }

    fun clearHistory() {
        c.engine.clearHistory()
        navMessage.value = null
    }

    class Factory(private val c: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(c) as T
    }

    private companion object {
        const val RECENT_EVENT_MS = 3_000L
        val ENERGY_CODES = com.salpim.core.model.WarningCodes.ENERGY.map { it.value }.toSet()
    }
}
