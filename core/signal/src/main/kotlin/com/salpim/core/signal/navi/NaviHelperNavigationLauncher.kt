package com.salpim.core.signal.navi

import android.content.Context
import com.salpim.core.model.NavResult
import com.salpim.core.model.NavigationLauncher
import com.salpim.core.model.ServiceDestination

/**
 * NaviHelper 어댑터. 목적지 설정 지원은 확인됨, 호출 방식은 미확인.
 * TODO(spike-S5): NaviHelper 목적지 설정 API, POI 검색 여부 확인 후 구현
 */
class NaviHelperNavigationLauncher(
    @Suppress("unused") private val context: Context,
) : NavigationLauncher {
    override val isAvailable: Boolean = false

    override suspend fun navigateTo(destination: ServiceDestination): NavResult = NavResult.Unsupported
}
