package com.salpim.core.model

/** 정비소 목적지. 사용자가 지정한 즐겨찾기 정비소 등 (S5 결과에 따라 POI 검색 추가 가능). */
data class ServiceDestination(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
)

sealed interface NavResult {
    data object Started : NavResult
    data object Unsupported : NavResult
    data class Failed(val reason: String) : NavResult
}

/**
 * 내비 목적지 설정 포트. 구현(NaviHelper 어댑터)은 core/signal 에만 둔다.
 */
interface NavigationLauncher {
    val isAvailable: Boolean
    suspend fun navigateTo(destination: ServiceDestination): NavResult

    /** 내비 자체 장소 검색 화면을 연다 (외부 키·위치 전송 없이 실제 장소를 찾는 경로) */
    suspend fun openSearch(keyword: String): NavResult
}
