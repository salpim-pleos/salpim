package com.salpim.feature.service

import com.salpim.core.model.NavResult
import com.salpim.core.model.NavigationLauncher
import com.salpim.core.model.ServiceDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 사용자가 지정한 즐겨찾기 정비소. (S5에서 NaviHelper POI 검색이 확인되면 검색으로 확장)
 * TODO(datastore): 영속화
 */
class FavoriteShopStore(initial: ServiceDestination? = null) {
    private val _shop = MutableStateFlow(initial)
    val shop: StateFlow<ServiceDestination?> = _shop.asStateFlow()
    fun set(value: ServiceDestination?) {
        _shop.value = value
    }
}

/** "정비소로 안내" 결과를 사용자에게 보여 줄 문장으로 정리한다. */
class ServiceNavigator(
    private val launcher: NavigationLauncher,
    private val store: FavoriteShopStore,
) {
    suspend fun navigateToFavorite(): String {
        val shop = store.shop.value ?: return "즐겨찾는 정비소를 먼저 지정해 주세요"
        return navigateTo(shop)
    }

    /** 내비 자체 검색 화면 (실제 장소, 외부 키 불필요) */
    suspend fun openNaviSearch(keyword: String): String {
        if (!launcher.isAvailable) return "내비 연동을 준비 중이에요"
        return when (val r = launcher.openSearch(keyword)) {
            NavResult.Started -> "내비에서 ‘$keyword’ 검색 화면을 열었어요"
            NavResult.Unsupported -> "내비 연동을 준비 중이에요"
            is NavResult.Failed -> "검색 화면을 열지 못했어요: ${r.reason}"
        }
    }

    suspend fun navigateTo(destination: ServiceDestination): String {
        if (!launcher.isAvailable) return "내비 연동을 준비 중이에요"
        return when (val r = launcher.navigateTo(destination)) {
            NavResult.Started -> "${destination.name}(으)로 안내를 시작했어요"
            NavResult.Unsupported -> "내비 연동을 준비 중이에요"
            is NavResult.Failed -> "안내를 시작하지 못했어요: ${r.reason}"
        }
    }
}
