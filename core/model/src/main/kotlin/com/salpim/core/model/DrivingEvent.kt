package com.salpim.core.model

enum class DrivingEventType { HARD_BRAKE, HARD_ACCEL, SHARP_TURN }

data class DrivingEvent(
    val type: DrivingEventType,
    val timestampMs: Long,
    /** HARD_BRAKE/ACCEL: km/h per second, SHARP_TURN: 조향각(deg) */
    val magnitude: Float,
)
