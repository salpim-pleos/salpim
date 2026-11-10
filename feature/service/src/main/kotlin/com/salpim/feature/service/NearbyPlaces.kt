package com.salpim.feature.service

import android.content.Context
import com.salpim.core.model.ServiceDestination
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** 앱이 찾아 주는 장소 종류 */
enum class PlaceCategory {
    /** 블루핸즈 정비 거점 */
    BLUE_HANDS,

    /** 주유소 */
    GAS_STATION,
}

/** 주변 장소 한 곳 */
data class NearbyPlace(
    val id: String,
    val category: PlaceCategory,
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
 * 주변 장소 검색 포트.
 *
 * 현재 구현은 [SampleNearbyPlaceRepository] (서울시청 기준 앱 내 샘플 목록, 화면에 "샘플 목록" 표시).
 * TODO(partner-data): 제휴 시 현대 블루핸즈 데이터·예약 API 구현으로 교체한다. 외부 지도 API와 위치 외부 전송은 쓰지 않는다
 *   (docs/proposal.md 3절). 실제 장소가 필요한 시연은 NaviHelper requestNavigationScreen(SearchPlace(...)) 검색 화면을 쓴다 (S5).
 */
interface NearbyPlaceRepository {
    /** true면 실시간 데이터가 아니므로 UI에 "샘플 목록"을 표시한다 */
    val isSample: Boolean

    suspend fun nearby(category: PlaceCategory, limit: Int = DEFAULT_LIMIT): List<NearbyPlace>

    companion object {
        const val DEFAULT_LIMIT = 5
    }
}

@Serializable
internal data class PlaceFile(val places: List<PlaceEntry>)

@Serializable
internal data class PlaceEntry(
    val id: String,
    val category: PlaceCategory,
    val name: String,
    val address: String,
    val phone: String? = null,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double? = null,
) {
    fun toPlace() = NearbyPlace(id, category, name, address, phone, latitude, longitude, distanceKm)
}

/**
 * 서울시청 주변 샘플 목록 (assets/nearby_places_sample.json). 실제 주소지만 실시간 데이터가 아니고
 * 거리는 서울시청에서 직선거리라 현재 위치와 무관하다. 출처와 확인 상태는 JSON `_note`.
 */
class SampleNearbyPlaceRepository internal constructor(
    private val places: List<NearbyPlace>,
) : NearbyPlaceRepository {
    override val isSample: Boolean = true

    override suspend fun nearby(category: PlaceCategory, limit: Int): List<NearbyPlace> =
        places.filter { it.category == category }
            .sortedBy { it.distanceKm ?: Double.MAX_VALUE }
            .take(limit)

    companion object {
        const val ASSET_NAME = "nearby_places_sample.json"
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(text: String): SampleNearbyPlaceRepository =
            SampleNearbyPlaceRepository(json.decodeFromString(PlaceFile.serializer(), text).places.map { it.toPlace() })

        fun fromAssets(context: Context): SampleNearbyPlaceRepository =
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
