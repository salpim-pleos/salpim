package com.salpim.app

import com.salpim.core.model.CarZone
import com.salpim.core.model.Explanation

/** 차 그림 주위에 보여 줄 부품 한 곳의 상태 */
data class PartStatus(
    val zone: CarZone,
    val name: String,
    val plainMeaning: String,
    /** null이면 정상 */
    val issue: Explanation?,
)

/** 부품 이름과 쉬운 설명. 차를 모르는 사람도 알 수 있게 */
object CarParts {
    private val parts = listOf(
        Triple(CarZone.ENGINE, "엔진오일", "엔진이 잘 돌도록 돕는 기름"),
        Triple(CarZone.BRAKE, "브레이크액", "차를 세우는 힘"),
        Triple(CarZone.TRANSMISSION, "변속기오일", "기어를 부드럽게"),
        Triple(CarZone.WASHER, "워셔액", "유리 닦는 물"),
        Triple(CarZone.AIRBAG, "에어백", "사고 때 몸을 보호"),
        Triple(CarZone.SEAT_BELT, "안전벨트", "사고 때 몸을 붙잡아 줌"),
    )

    /** 부위마다 가장 급한 문제를 붙인다. 문제가 있는 부위가 먼저 온다 */
    fun statuses(activeWarnings: List<Explanation>): List<PartStatus> = parts
        .map { (zone, name, meaning) ->
            val issue = activeWarnings.filter { it.zone == zone }.minByOrNull { it.urgency.ordinal }
            PartStatus(zone, issue?.title ?: name, issue?.plainMeaning ?: meaning, issue)
        }
        .sortedBy { it.issue?.urgency?.ordinal ?: Int.MAX_VALUE }
}
