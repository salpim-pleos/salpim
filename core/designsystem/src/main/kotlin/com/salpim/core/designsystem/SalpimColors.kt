package com.salpim.core.designsystem

import androidx.compose.ui.graphics.Color

/**
 * 색 토큰. 출처: Pleos Design Guide (basic, regulation) + 현대 웹 액션 색. docs/DESIGN.md 와 같은 값.
 * 평상시 화면은 흑백·회색, 상태 색(urgent/caution/notice/ok)은 상태가 있을 때만 쓴다.
 */
object SalpimColors {
    val Background = Color(0xFF131417) // Pleos basic_00
    val Surface = Color(0xFF1D1E21) // Pleos basic_50
    val SurfaceRaised = Color(0xFF313236) // Pleos basic_100
    val Line = Color(0xFF313236)
    val Track = Color(0xFF44464E) // Pleos basic_200

    val Text = Color(0xFFF7F8FA) // Pleos basic_800
    val TextSoft = Color(0xFFC7C9CE) // Pleos basic_500
    val TextSecondary = Color(0xFFA4A8AE) // Pleos basic_400
    val TextTertiary = Color(0xFF686A72) // Pleos basic_300 — 큰 글자에만

    val Action = Color(0xFF002C5F) // Hyundai navy
    val OnAction = Color(0xFFFFFFFF)
    val Selected = Color(0xFF007FA8) // Hyundai teal — 면/선에만

    val Urgent = Color(0xFFFF4339) // Pleos regulation red
    val Caution = Color(0xFFFFC224) // Pleos regulation yellow
    val Notice = Color(0xFF0082FF) // Pleos regulation blue
    val Ok = Color(0xFF55F165) // Pleos regulation green
    val Simulated = Color(0xFFFF8A00) // Pleos regulation orange
}
