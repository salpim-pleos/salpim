package com.salpim.core.signal.navi

import ai.pleos.playground.navi.constants.RouteOption
import ai.pleos.playground.navi.data.NavigationScreenRequest
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.helper.NaviHelper
import android.content.Context
import android.util.Log
import com.salpim.core.model.NavResult
import com.salpim.core.model.NavigationLauncher
import com.salpim.core.model.ServiceDestination

/**
 * NaviHelper(ai.pleos.playground:NaviHelper 3.0.5) 어댑터 [확인됨: AAR 시그니처].
 * - 목적지: `requestRoute(RouteInfo)` (권한 NAVI_ROUTE_SEARCH). 호출 성공이 경로 탐색 완료를 뜻하지는 않는다 (문서)
 * - 검색 화면: `requestNavigationScreen(SearchPlace(keyword))` (권한 NAVI_CUSTOM_MAP)
 * 연결 전·실패 시에는 [isAvailable]이 false라 호출하지 않는다 (내비 미장착 차량 포함).
 */
class NaviHelperNavigationLauncher(context: Context) : NavigationLauncher {
    private val helper = NaviHelper(context.applicationContext)

    @Volatile
    private var connected = false

    override val isAvailable: Boolean get() = connected

    /** 앱 시작 시 한 번. 결과는 비동기로 [isAvailable]에 반영된다 */
    fun connect() {
        helper.initialize(
            { connected = true },
            { e ->
                connected = false
                Log.w(TAG, "NaviHelper unavailable", e)
            },
        )
    }

    fun disconnect() {
        if (connected) helper.release()
        connected = false
    }

    override suspend fun navigateTo(destination: ServiceDestination): NavResult {
        if (!connected) return NavResult.Unsupported
        helper.requestRoute(
            RouteInfo(
                latitude = destination.latitude,
                longitude = destination.longitude,
                displayLatitude = null,
                displayLongitude = null,
                poiName = destination.name,
                poiId = "", // TODO(spike-S5): poiId 없이 경로 요청이 되는지 에뮬레이터에서 확인
                address = destination.address.orEmpty(),
                poiSubId = "",
                routeOption = RouteOption.RECOMMENDED,
                poiProvider = "",
                isMobileRequest = false,
            ),
        )
        return NavResult.Started
    }

    override suspend fun openSearch(keyword: String): NavResult {
        if (!connected) return NavResult.Unsupported
        helper.requestNavigationScreen(NavigationScreenRequest.SearchPlace(keyword))
        return NavResult.Started
    }

    private companion object {
        const val TAG = "SalpimNavi"
    }
}
