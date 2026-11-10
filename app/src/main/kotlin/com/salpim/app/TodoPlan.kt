package com.salpim.app

import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.core.model.WarningCode
import com.salpim.feature.fuel.RefuelAdvice

/** 정차 화면 "지금 할 일" 한 가지 */
sealed interface Todo {
    val key: TodoKey

    data class Service(val explanation: Explanation) : Todo {
        override val key = TodoKey.Warning(explanation.code)
    }

    data class Refuel(val fuel: FuelSummary, val advice: RefuelAdvice) : Todo {
        override val key = TodoKey.Refuel
    }
}

/** 사용자가 고른 할 일 (터치·음성) */
sealed interface TodoKey {
    data class Warning(val code: WarningCode) : TodoKey

    data object Refuel : TodoKey
}

/** 정차 화면 오른쪽 패널에 한 번에 하나만 보여 준다 (스크롤 없이 읽히게) */
sealed interface PanelMode {
    data object Places : PanelMode

    data object Reservation : PanelMode

    data class Doing(val todo: Todo, val all: List<Todo>) : PanelMode

    data object Nothing : PanelMode
}

/**
 * 할 일 순서와 패널 선택. 판단만 하는 순수 함수.
 *
 * 순서 [설계]: 즉시 정차 경고 → 지금 주유 → 점검 권장 경고 → 가는 길에 주유 → 안내.
 * 칩으로 고르게 하므로 [MAX_TODOS]개까지만 보인다.
 */
object TodoPlan {
    const val MAX_TODOS = 3

    fun todos(warnings: List<Explanation>, fuel: FuelSummary?, advice: RefuelAdvice): List<Todo> {
        val refuel = fuel?.takeIf { advice.showsStations }?.let { Todo.Refuel(it, advice) }
        return (warnings.map(Todo::Service) + listOfNotNull(refuel))
            .sortedBy(::rank)
            .take(MAX_TODOS)
    }

    fun panel(todos: List<Todo>, selected: TodoKey?, showingPlaces: Boolean, showingReservation: Boolean): PanelMode = when {
        showingPlaces -> PanelMode.Places
        showingReservation -> PanelMode.Reservation
        todos.isEmpty() -> PanelMode.Nothing
        else -> PanelMode.Doing(todos.firstOrNull { it.key == selected } ?: todos.first(), todos)
    }

    private fun rank(todo: Todo): Int = when (todo) {
        is Todo.Service -> when (todo.explanation.urgency) {
            Urgency.IMMEDIATE_STOP -> RANK_STOP
            Urgency.SERVICE_SOON -> RANK_SERVICE
            Urgency.INFO -> RANK_INFO
        }
        is Todo.Refuel -> if (todo.advice is RefuelAdvice.RefuelNow) RANK_REFUEL_NOW else RANK_REFUEL_ON_THE_WAY
    }

    private const val RANK_STOP = 0
    private const val RANK_REFUEL_NOW = 1
    private const val RANK_SERVICE = 2
    private const val RANK_REFUEL_ON_THE_WAY = 3
    private const val RANK_INFO = 4
}
