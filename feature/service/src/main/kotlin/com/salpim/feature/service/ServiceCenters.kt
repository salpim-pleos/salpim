package com.salpim.feature.service

import android.content.Context
import com.salpim.core.model.ServiceDestination
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** 정비 거점(블루핸즈) 한 곳 */
data class ServiceCenter(
    val id: String,
    val name: String,
    val address: String,
    val phone: String?,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double?,
) {
    fun toDestination() = ServiceDestination(name, latitude, longitude, address)
}

/**
 * 주변 정비 거점 검색 포트.
 *
 * 현재 구현은 [MockServiceCenterRepository] (앱 내 예시 목록).
 * TODO(kakao-local): KakaoLocalServiceCenterRepository — 카카오 로컬 API 키워드 검색
 *   ("블루핸즈", 현재 좌표 x=경도/y=위도, radius m). REST 키는 local.properties, 정차 중 사용자 요청 시에만 호출,
 *   실패 시 NaviHelper requestNavigationScreen(SearchPlace("블루핸즈")) 폴백 (docs/spikes/S5.md)
 * TODO(location): 현재 위치 소스 확정 (FusedLocation isSupported 또는 Android 위치) — docs/spikes/S6.md
 */
interface ServiceCenterRepository {
    /** true면 UI에 "예시 데이터"를 표시한다 */
    val isMock: Boolean

    suspend fun nearby(limit: Int = 5): List<ServiceCenter>
}

@Serializable
internal data class CenterFile(val centers: List<CenterEntry>)

@Serializable
internal data class CenterEntry(
    val id: String,
    val name: String,
    val address: String,
    val phone: String? = null,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double? = null,
)

/** 예시 목록. 실제 지점 정보가 아니다. */
class MockServiceCenterRepository internal constructor(private val centers: List<ServiceCenter>) : ServiceCenterRepository {
    override val isMock: Boolean = true

    override suspend fun nearby(limit: Int): List<ServiceCenter> =
        centers.sortedBy { it.distanceKm ?: Double.MAX_VALUE }.take(limit)

    companion object {
        const val ASSET_NAME = "bluehands_mock.json"
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(text: String): MockServiceCenterRepository =
            MockServiceCenterRepository(
                json.decodeFromString(CenterFile.serializer(), text).centers.map {
                    ServiceCenter(it.id, it.name, it.address, it.phone, it.latitude, it.longitude, it.distanceKm)
                },
            )

        fun fromAssets(context: Context): MockServiceCenterRepository =
            context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).use { fromJson(it.readText()) }
    }
}

/**
 * 휴대폰 예약 연결. 앱 안 예약 API는 없으므로 공식 예약 페이지를 QR로 넘긴다.
 * TODO(qr): QR 이미지 렌더링 (예: ZXing core, Apache-2.0)
 * TODO(partner-api): 블루핸즈/마이현대 제휴 예약 API가 열리면 정비 항목을 담아 앱 내 예약
 */
object ReservationLink {
    /** 현대 공식 "서비스 네트워크 검색/예약" 페이지 (2026-11-09 확인, 주소 변경 가능) */
    const val OFFICIAL_RESERVATION_URL = "https://www.hyundai.com/kr/ko/customer-service/service-network/service-reservation-search"
}
