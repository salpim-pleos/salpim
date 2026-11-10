package com.salpim.core.signal.sim

import com.salpim.core.model.NavResult
import com.salpim.core.model.NavigationLauncher
import com.salpim.core.model.ServiceDestination

/**
 * 시뮬레이션 신호일 때의 내비. 실제 내비가 없는 일반 에뮬레이터 시연에서 길 안내가 "시작된" 흐름을 보여 준다.
 * 화면에는 시뮬레이션 표시가 항상 함께 나온다 (AGENTS.md 2절). 실제 차량에서는 NaviHelper 어댑터를 쓴다.
 */
class SimulatedNavigationLauncher : NavigationLauncher {
    override val isAvailable: Boolean = true

    override suspend fun navigateTo(destination: ServiceDestination): NavResult = NavResult.Started

    override suspend fun openSearch(keyword: String): NavResult = NavResult.Started
}
