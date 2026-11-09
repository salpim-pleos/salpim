package com.salpim.app.ui

import com.salpim.core.model.WarningCode
import com.salpim.feature.service.NearbyPlace
import com.salpim.feature.service.PlaceCategory

/** 화면에서 일어나는 사용자 조작. 터치와 음성이 같은 동작으로 이어진다 */
interface MainActions {
    fun onMic()
    fun onHint(text: String)
    fun selectWarning(code: WarningCode)
    fun findNearby(category: PlaceCategory)
    fun routeTo(place: NearbyPlace)
    fun showReservation()
    fun playScenario(id: String)
    fun clearHistory()
}
