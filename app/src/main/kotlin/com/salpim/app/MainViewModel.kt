package com.salpim.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.salpim.app.ui.MainActions
import com.salpim.app.voice.NoticeSnapshot
import com.salpim.app.voice.ProactiveNotice
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
    /** 내비 목적지 이름 (경로가 아직 안 잡혔어도) */
    val destinationName: String? = null,
    /** 시연용 내비 그림이 주행거리만큼 움직인다 */
    val odometerKm: Double? = null,
    val refuel: RefuelAdvice = RefuelAdvice.Unknown,
    val report: CoachReport = CoachReport.from(emptyList()),
    /** 정차 화면 할 일 (급한 순) */
    val todos: List<Todo> = emptyList(),
    /** 정차 화면 오른쪽 패널에 지금 보여 줄 것 */
    val panel: PanelMode = PanelMode.Nothing,
    val inputs: UserInputs = UserInputs(),
    val voicePhase: VoicePhase = VoicePhase.IDLE,
    val partialText: String? = null,
    val scenarios: List<ScenarioInfo> = emptyList(),
    val playingScenario: String? = null,
)

/** 사용자 조작(터치·음성)으로 바뀌는 화면 상태 */
data class UserInputs(
    val selectedTodo: TodoKey? = null,
    val navMessage: String? = null,
    /** 찾은 장소. null이면 아직 안 찾음 */
    val places: List<NearbyPlace>? = null,
    val placesCategory: PlaceCategory? = null,
    val placesSample: Boolean = false,
    val showReservation: Boolean = false,
    /** 앱이 마지막으로 한 말 (화면에도 남긴다) */
    val lastReply: String? = null,
    /** 그 말에 앞서 운전자가 한 말. 앱이 먼저 말을 걸었으면 null */
    val lastHeard: String? = null,
    /** 가는 길에 들르기로 한 곳 (내비 경유지) */
    val waypoint: String? = null,
)

/** 장소 목록을 열면 예약 QR·이전 안내 문구는 닫는다 (패널에는 한 가지만) */
private fun UserInputs.showingPlaces(places: List<NearbyPlace>, category: PlaceCategory) =
    copy(places = places, placesCategory = category, showReservation = false, navMessage = null)

class MainViewModel(private val c: AppContainer) : ViewModel(), MainActions {
    private val inputs = MutableStateFlow(UserInputs(placesSample = c.places.isSample))
    private val scenarios = scenarioInfos()

    private val assistant = VoiceAssistant()
    private val dialog = VoiceDialog()
    private var dialogContext: DialogContext = DialogContext.Idle
    private var pendingEffect: VoiceEffect? = null
    private val voice = VoiceController(viewModelScope, c.speechInput, c.speechOutput, onHeard = ::handleHeard)

    init {
        viewModelScope.launch { watchForNotices() }
    }

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

    override fun selectWarning(code: WarningCode) = selectTodo(TodoKey.Warning(code))

    override fun selectTodo(key: TodoKey) = inputs.update {
        it.copy(selectedTodo = key, places = null, showReservation = false)
    }

    override fun closePanel() = inputs.update { it.copy(places = null, showReservation = false, navMessage = null) }

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
        inputs.update { it.copy(lastHeard = text) }
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

    /** 경고가 새로 켜지거나 주유가 필요해진 순간 앱이 먼저 한 줄 (ProactiveNotice) */
    private suspend fun watchForNotices() {
        var previous: NoticeSnapshot? = null
        c.engine.state.collect { engine ->
            val now = NoticeSnapshot(engine.activeWarnings, engine.refuel)
            previous?.let { ProactiveNotice.between(it, now) }?.let(::announce)
            previous = now
        }
    }

    /**
     * 앱이 먼저 거는 말은 화면 글자로만 보여 준다.
     * TODO(audio-focus): 내비 안내 음성과 겹치지 않는 Audio Focus 가이드 확인 후 소리로도 (AGENTS.md 3절)
     */
    private fun announce(reply: VoiceReply) {
        inputs.update { it.copy(lastHeard = null, lastReply = reply.text) }
        pendingEffect = reply.pending
        dialogContext = reply.nextContext
    }

    private fun runEffect(effect: VoiceEffect) {
        when (effect) {
            is VoiceEffect.FindPlaces -> viewModelScope.launch {
                val places = c.places.nearby(effect.category)
                inputs.update { it.showingPlaces(places, effect.category) }
                applyReply(assistant.describePlaces(effect.category, places, onRoute = ui.value.route != null))
            }
            is VoiceEffect.RouteTo -> viewModelScope.launch {
                val message = c.serviceNavigator.navigateTo(effect.place.toDestination())
                // 주행 중에 고른 곳은 목록을 닫는다 (정차하면 목록이 아니라 할 일이 먼저 보이게)
                val keepList = !ui.value.policy.restricted
                inputs.update {
                    it.copy(navMessage = message, waypoint = effect.place.name, places = it.places.takeIf { keepList })
                }
            }
            is VoiceEffect.StartRoute -> startRoute(effect.destination)
            VoiceEffect.ShowReservation -> inputs.update { it.copy(showReservation = true, places = null) }
            VoiceEffect.ClearHistory -> clearHistory()
        }
    }

    /** 시뮬레이션이면 그 목적지 시연 시나리오, 아니면 내비 검색 화면 (외부 지도 API 없이 실제 장소) */
    private fun startRoute(destination: String) {
        inputs.update { it.copy(waypoint = null, places = null, showReservation = false) }
        val scenario = c.simulated?.let { Scenarios.forDestination(destination) }
        if (scenario != null) {
            c.simulated?.play(scenario)
            return
        }
        viewModelScope.launch {
            val message = c.serviceNavigator.openNaviSearch(destination)
            inputs.update { it.copy(navMessage = message) }
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
        val fuel = FuelText.summary(engine.refuel)
        val todos = TodoPlan.todos(warnings, fuel, engine.refuel)
        return MainUiState(
            drivingState = engine.drivingState,
            policy = UxPolicy.resolve(engine.drivingState, engine.ux),
            simulated = c.simulated != null,
            topWarning = engine.activeWarnings.firstOrNull(),
            parts = CarParts.statuses(engine.activeWarnings),
            fuel = fuel,
            route = RouteProgress.from(engine.signal),
            destinationName = engine.signal?.destinationName,
            odometerKm = engine.signal?.odometerKm,
            refuel = engine.refuel,
            report = CoachReport.from(engine.events),
            todos = todos,
            panel = TodoPlan.panel(
                todos,
                userInputs.selectedTodo,
                userInputs.places != null,
                userInputs.showReservation,
            ),
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
