package com.salpim.core.model

/** 앱 역할 전환의 기준이 되는 주행 상태. */
enum class DrivingState {
    /** 기어 P 또는 주차 브레이크 체결, 정지 */
    PARKED,

    /** 정지했지만 주차 상태는 아님 (신호 대기 등) */
    STOPPED,

    /** 주행 중. 7절 안전 규칙이 가장 엄격하게 적용된다 */
    DRIVING,
}
