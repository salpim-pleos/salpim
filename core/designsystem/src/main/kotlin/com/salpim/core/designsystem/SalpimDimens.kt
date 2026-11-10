package com.salpim.core.designsystem

import androidx.compose.ui.unit.dp

/**
 * 간격·크기 토큰. 8dp 격자: 화면 여백 24, 카드 사이 16, 카드 안 요소 사이 12.
 * 화면은 남는 높이를 weight로 나눠 쓴다 — 고정 높이를 쌓아 넘치면 잘리거나 스크롤이 생긴다.
 */
object SalpimDimens {
    /** 터치 최소 크기 (Android for Cars 76dp) */
    val TouchMin = 76.dp
    val PrimaryButtonHeight = 80.dp
    val ScreenPadding = 24.dp
    val SectionGap = 16.dp
    val ItemGap = 12.dp
    val SmallGap = 8.dp
    val Line = 2.dp
    val ListRowHeight = 80.dp

    /** 주행 중 문제 부위 아이콘 (멀리서 한눈에) */
    val HeroIconBox = 132.dp
    val HeroIcon = 96.dp

    /** 연료·점수 카드 아이콘 */
    val LineIcon = 48.dp

    /** 정차 화면 부품 타일 아이콘 */
    val TileIcon = 48.dp

    /** 음성 바 높이 고정: 마이크 + "나" 한 줄 + "살핌" 두 줄. 대화가 길어져도 위 화면이 밀리지 않는다 */
    val VoiceBarHeight = 120.dp

    /** 글줄 앞 작은 아이콘 */
    val SmallIcon = 24.dp

    /** 버튼 안 아이콘 */
    val ButtonIcon = 36.dp
    val ButtonIconGap = 16.dp
    val BarHeight = 20.dp
    val StatusDot = 20.dp

    /** 휴대폰으로 찍는 예약 QR. 흰 여백(quiet zone) 포함 */
    val QrSize = 184.dp
    val QrQuietZone = 12.dp
}
