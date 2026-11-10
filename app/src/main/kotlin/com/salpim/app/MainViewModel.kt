package com.salpim.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.salpim.app.ui.MainActions
import com.salpim.app.voice.VoiceAssistant
import com.salpim.app.voice.VoiceController
import com.salpim.app.voice.VoiceEffect
import com.salpim.app.voice.VoiceFacts
import com.salpim.app.voice.VoiceReply
import com.salpim.core.designsystem.VoicePhase
import com.salpim.core.model.DrivingState
import com.salpim.core.model.Explanation
import com.salpim.core.model.UxPolicy
import com.salpim.core.model.UxRestrictionFlags
import com.salpim.core.model.WarningCode
import com.salpim.core.signal.sim.Scenarios
import com.salpim.feature.coach.CoachReport
import com.salpim.feature.fuel.RefuelAdvice
import com.salpim.feature.service.NearbyPlace
import com.salpim.feature.service.PlaceCategory
import com.salpim.feature.voice.DialogContext
import com.salpim.feature.voice.VoiceDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScenarioInfo(val id: String, val title: String)

data class MainUiState(
    val drivingState: DrivingState = DrivingState.PARKED,
    val policy: UxPolicy = UxPolicy.resolve(DrivingState.PARKED, UxRestrictionFlags.NONE),
    val simulated: Boolean = false,
    /** 지금 켜진 경고 중 가장 급한 것 */
    val topWarning: Explanation? = null,
    val parts: List<PartStatus> = CarParts.statuses(emptyList()),
    val fuel: FuelSummary? = null,
    /** 내비 경로 안내 중인 목적지. 없으면 null */
    val route: RouteProgress? = null,
    val refuel: RefuelAdvice = RefuelAdvice.Unknown,
    val report: CoachReport = CoachReport.from(emptyList()),
    /** 정차 화면 목록: 켜진 경고 + 오늘 받은 경고 */
    val warningList: List<Explanation> = emptyList(),
    val selected: Explanation? = null,
    val inputs: UserInputs = UserInputs(),
    val voicePhase: VoicePhase = VoicePhase.IDLE,
    val partialText: String? = null,
    val scenarios: List<ScenarioInfo> = emptyList(),
    val playingScenario: String? = null,
)

/** 사용자 조작(터치·음성)으로 바뀌는 화면 상태 */
data class UserInputs(
    val selectedCode: WarningCode? = null,
    val navMessage: String? = null,
    /** 찾은 장소. null이면 아직 안 찾음 */
    val places: List<NearbyPlace>? = null,
    val placesCategory: PlaceCategory? = null,
    val placesSample: Boolean = false,
    val showReservation: Boolean = false,
    /** 앱이 마지막으로 한 말 (화면에도 남긴다) */
    val lastReply: String? = null,
)

class MainViewModel(private val c: AppContainer) : ViewModel(), MainActions {
    private val inputs = MutableStateFlow(UserInputs(placesSample = c.places.isSample))
    private val scenarios = scenarioInfos()

    private val assistant = VoiceAssistant()
    private val dialog = VoiceDialog()
    private var dialogContext: DialogContext = DialogContext.Idle
    private var pendingEffect: VoiceEffect? = null
    private val voice = VoiceController(viewModelScope, c.speechInput, c.speechOutput, onHeard = ::handleHeard)

    val ui: StateFlow<MainUiState> = combine(
        c.engine.state,
        inputs,
        c.simulated?.playing ?: flowOf(null),
        voice.phase,
        voice.partialText,
    ) { engine, userInputs, playing, phase, partial -> buildState(engine, userInputs, playing, phase, partial) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            MainUiState(simulated = c.simulated != null),
        )

    override fun onMic() = voice.toggleListening()

    /** 음성 예시 칩은 그 말을 한 것과 같다 (마이크 없이도 같은 흐름을 쓴다) */
    override fun onHint(text: String) = handleHeard(text)

    override fun selectWarning(code: WarningCode) = inputs.update { it.copy(selectedCode = code) }

    override fun findNearby(category: PlaceCategory) = runEffect(VoiceEffect.FindPlaces(category))

    override fun routeTo(place: NearbyPlace) = runEffect(VoiceEffect.RouteTo(place))

    override fun showReservation() = runEffect(VoiceEffect.ShowReservation)

    override fun playScenario(id: String) {
        val sim = c.simulated ?: return
        Scenarios.all.firstOrNull { it.id == id }?.let(sim::play)
    }

    override fun clearHistory() {
        c.engine.clearHistory()
        inputs.update { it.copy(navMessage = null) }
    }

    private fun handleHeard(text: String) {
        val reply = assistant.reply(dialog.resolve(text, dialogContext), currentFacts(), pendingEffect)
        applyReply(reply)
    }

    private fun applyReply(reply: VoiceReply) {
        inputs.update { it.copy(lastReply = reply.text) }
        pendingEffect = reply.pending
        dialogContext = reply.nextContext
        voice.say(reply.text)
        reply.effect?.let(::runEffect)
    }

    private fun runEffect(effect: VoiceEffect) {
        when (effect) {
            is VoiceEffect.FindPlaces -> viewModelScope.launch {
                val places = c.places.nearby(effect.category)
                inputs.update { it.copy(places = places, placesCategory = effect.category) }
                applyReply(assistant.describePlaces(effect.category, places))
            }
            is VoiceEffect.RouteTo -> viewModelScope.launch {
                val message = c.serviceNavigator.navigateTo(effect.place.toDestination())
                inputs.update { it.copy(navMessage = message) }
            }
            VoiceEffect.ShowReservation -> inputs.update { it.copy(showReservation = true) }
            VoiceEffect.ClearHistory -> clearHistory()
        }
    }

    private fun currentFacts(): VoiceFacts {
        val state = ui.value
        return VoiceFacts(
            topWarning = state.topWarning,
            fuel = state.refuel,
            score = state.report.score,
            eventCounts = state.report.counts,
            shownPlaces = state.inputs.places.orEmpty(),
        )
    }

    private fun buildState(
        engine: EngineState,
        userInputs: UserInputs,
        playing: String?,
        phase: VoicePhase,
        partial: String?,
    ): MainUiState {
        val warnings = (engine.activeWarnings.map { it.code } + engine.seenWarnings).distinct().map(
            c.explainer::explain,
        )
        return MainUiState(
            drivingState = engine.drivingState,
            policy = UxPolicy.resolve(engine.drivingState, engine.ux),
            simulated = c.simulated != null,
            topWarning = engine.activeWarnings.firstOrNull(),
            parts = CarParts.statuses(engine.activeWarnings),
            fuel = FuelText.summary(engine.refuel),
            route = RouteProgress.from(engine.signal),
            refuel = engine.refuel,
            report = CoachReport.from(engine.events),
            warningList = warnings,
            selected = warnings.firstOrNull { it.code == userInputs.selectedCode } ?: warnings.firstOrNull(),
            inputs = userInputs,
            voicePhase = phase,
            partialText = partial,
            scenarios = scenarios,
            playingScenario = playing,
        )
    }

    private fun scenarioInfos(): List<ScenarioInfo> =
        if (c.simulated == null) emptyList() else Scenarios.all.map { ScenarioInfo(it.id, it.title) }

    class Factory(private val c: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(c) as T
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
