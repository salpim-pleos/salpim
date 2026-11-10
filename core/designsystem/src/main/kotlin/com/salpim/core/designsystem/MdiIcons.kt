package com.salpim.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Material Design Icons (Pictogrammers, 아이콘은 Apache-2.0 — ICONS-LICENSE-MDI.txt).
 * 원본 SVG의 path를 그대로 옮겼다. 색은 쓰는 쪽에서 tint로 준다.
 * 실제 계기판 경고등과 같은 모양을 피하려고 부품·동작을 뜻하는 아이콘을 골랐다 (docs/DESIGN.md).
 */
object MdiIcons {
    private const val GRID = 24f

    private fun icon(name: String, pathData: String): ImageVector =
        ImageVector.Builder(name, GRID.dp, GRID.dp, GRID, GRID)
            .addPath(addPathNodes(pathData), fill = SolidColor(Color.White))
            .build()

    /** mdi:oil */
    val Oil: ImageVector = icon(
        "Oil",
        "M22 12.5s2 2.17 2 3.5a2 2 0 0 1-2 2a2 2 0 0 1-2-2c0-1.33 2-3.5 2-3.5M6 6h4a1 1 0 0 1 1 1a1 1 0 0 1-1 1H9v2h2c." +
            "74 0 1.39.4 1.73 1l6.51-3.76l3.26 1.89c.5.27.64.87.37 1.37c-.28.47-.87.64-1.37.36l-2.1-1.21l-3.65 6.32c-.34.61" +
            "-1 1.03-1.75 1.03H5a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h2V8H6a1 1 0 0 1-1-1a1 1 0 0 1 1-1m-1 6v3h9l2.06-3.57l-3.46 " +
            "2l-.91-1.43zM.38 9.21L2.09 7.5c.41-.39 1.02-.39 1.41 0s.39 1 0 1.41l-1.71 1.71c-.39.38-1.02.38-1.41 0C0 10.23 " +
            "0 9.6.38 9.21",
    )

    /** mdi:engine */
    val Engine: ImageVector = icon(
        "Engine",
        "M7 4v2h3v2H7l-2 2v3H3v-3H1v8h2v-3h2v3h3l2 2h8v-4h2v3h3V9h-3v3h-2V8h-6V6h3V4z",
    )

    /** mdi:car-brake-fluid-level */
    val BrakeFluid: ImageVector = icon(
        "BrakeFluid",
        "M1.5 12c0 2.9 1.18 5.5 3.08 7.42L3.5 20.5C1.34 18.31 0 15.31 0 12s1.34-6.31 3.5-8.5l1.08 1.08C2.68 6.5 1.5 9.1" +
            "1 1.5 12M21 12a9 9 0 1 1-18.001-.001A9 9 0 0 1 21 12M5 12c0 .44.05.86.12 1.28c.18-.19.34-.28.66-.28c.52 0 .61." +
            "21 1 .71S7.85 15 8.89 15c1.03 0 1.72-.79 2.11-1.29c.26-.34.39-.53.61-.63c.1-.05.22-.08.39-.08c.5 0 .61.21 1 .7" +
            "1S14.08 15 15.11 15c1.04 0 1.72-.79 2.11-1.29s.48-.71 1-.71c.32 0 .48.09.66.28c.07-.42.12-.84.12-1.28c0-3.86-3" +
            ".14-7-7-7s-7 3.14-7 7m15.5-8.5l-1.08 1.08C21.32 6.5 22.5 9.11 22.5 12c0 2.9-1.18 5.5-3.08 7.42l1.08 1.08c2.16-" +
            "2.19 3.5-5.19 3.5-8.5s-1.34-6.31-3.5-8.5",
    )

    /** mdi:car-shift-pattern */
    val Transmission: ImageVector = icon(
        "Transmission",
        "M8 5H4V2h4zM4 22h4v-3H4zM14 2h-4v3h4zm-4 20h4v-3h-4zm6-20v3h4V2zm1 9h-4V7h-2v4H7V7H5v10h2v-4h4v4h2v-4h6V7h-2z",
    )

    /** mdi:wiper-wash */
    val WasherFluid: ImageVector = icon(
        "WasherFluid",
        "M13 6c0-.3.1-1.4.8-2.2L12 2.4l-1.8 1.5c.7.7.8 1.8.8 2.1c-6.3.4-9 5-9 5l7 7s.7-1.3 2-1.8v2.1c-.6.3-1 1-1 1.7a2 " +
            "2 0 0 0 2 2a2 2 0 0 0 2-2c0-.7-.4-1.4-1-1.7v-2.1c1.3.5 2 1.8 2 1.8l7-7s-2.7-4.5-9-5m-2 8.1c-.8.2-1.5.5-2.1 1l-" +
            "4.2-4.2C5.8 9.8 7.8 8.3 11 8.1zm4.1 1c-.6-.4-1.3-.8-2.1-1v-6c3.2.3 5.2 1.7 6.3 2.8zM18 1.3l-.7 1.9c-.7-.3-1.8-" +
            ".3-2.6 0L14 1.3c1.2-.4 2.8-.4 4 0M21 6h-2s0-1.3-.8-2.1l1.5-1.3C21 4 21 5.9 21 6M4.2 2.6l1.5 1.3C5 4.7 5 6 5 6H" +
            "3c0-.1 0-2 1.2-3.4M10 1.3l-.7 1.9c-.7-.3-1.8-.3-2.6 0L6 1.3c1.2-.4 2.8-.4 4 0",
    )

    /** mdi:seatbelt */
    val Seatbelt: ImageVector = icon(
        "Seatbelt",
        "M12 2a2 2 0 0 1 2 2c0 1.11-.89 2-2 2a2 2 0 0 1-2-2a2 2 0 0 1 2-2m.39 12.79a34 34 0 0 1 4.25.25c.06-2.72-.18-5." +
            "12-.64-6.04c-.13-.27-.31-.5-.5-.7l-8.07 6.92c1.36-.22 3.07-.43 4.96-.43M7.46 17c.13 1.74.39 3.5.81 5h2.07c-.29" +
            "-.88-.5-1.91-.66-3c0 0 2.32-.44 4.64 0c-.16 1.09-.37 2.12-.66 3h2.07c.44-1.55.7-3.39.83-5.21a35 35 0 0 0-4.17-" +
            ".25c-1.93 0-3.61.21-4.93.46M12 7S9 7 8 9c-.34.68-.56 2.15-.63 3.96l6.55-5.62C12.93 7 12 7 12 7m6.57-1.33l-1.14" +
            "-1.33l-3.51 3.01c.55.19 1.13.49 1.58.95zm2.1 10.16c-.09-.03-1.53-.5-4.03-.79c-.01.57-.04 1.16-.08 1.75c2.25.28" +
            " 3.54.71 3.56.71zm-13.3-2.87l-3.94 3.38l.89 1.48c.02-.01 1.18-.46 3.14-.82c-.11-1.41-.14-2.8-.09-4.04",
    )

    /** mdi:airbag */
    val Airbag: ImageVector = icon(
        "Airbag",
        "M14 8a5 5 0 0 1-5 5a5 5 0 0 1-5-5a5 5 0 0 1 5-5a5 5 0 0 1 5 5m-3.54 7.55L13 18.03l-2 .02l-3.5 3.53L6 20.09zM17" +
            " 2c1.08 0 2 .88 2 2c0 1.08-.88 2-2 2c-1.08 0-2-.88-2-2c0-1.08.89-2 2-2m-2.59 13h-2.82l5.7 5.71l1.42-1.42zm.71-" +
            ".71l4.29 4.3l.22.21c.23-.38.37-.8.37-1.3v-8A2.5 2.5 0 0 0 17.5 7A2.5 2.5 0 0 0 15 9.5v4.67z",
    )

    /** mdi:gas-station */
    val GasStation: ImageVector = icon(
        "GasStation",
        "M18 10a1 1 0 0 1-1-1a1 1 0 0 1 1-1a1 1 0 0 1 1 1a1 1 0 0 1-1 1m-6 0H6V5h6m7.77 2.23l.01-.01l-3.72-3.72L15 4.56" +
            "l2.11 2.11C16.17 7 15.5 7.93 15.5 9a2.5 2.5 0 0 0 2.5 2.5c.36 0 .69-.08 1-.21v7.21a1 1 0 0 1-1 1a1 1 0 0 1-1-1" +
            "V14a2 2 0 0 0-2-2h-1V5a2 2 0 0 0-2-2H6c-1.11 0-2 .89-2 2v16h10v-7.5h1.5v5A2.5 2.5 0 0 0 18 21a2.5 2.5 0 0 0 2." +
            "5-2.5V9c0-.69-.28-1.32-.73-1.77",
    )

    /** mdi:car-wrench */
    val CarWrench: ImageVector = icon(
        "CarWrench",
        "M20.96 16.45c.01-.15.04-.3.04-.45v.5zM11 16c0 .71.15 1.39.42 2H6v1c0 .55-.45 1-1 1H4c-.55 0-1-.45-1-1v-8l2.08-" +
            "6c.2-.58.76-1 1.42-1h11c.66 0 1.22.42 1.42 1L21 11v5c0-2.76-2.24-5-5-5s-5 2.24-5 5m-3-2.5c0-.83-.67-1.5-1.5-1." +
            "5S5 12.67 5 13.5S5.67 15 6.5 15S8 14.33 8 13.5M19 10l-1.5-4.5h-11L5 10zm3.87 11.19l-4.11-4.11c.41-1.04.18-2.26" +
            "-.68-3.11c-.9-.91-2.25-1.09-3.34-.59l1.94 1.94l-1.35 1.36l-1.99-1.95c-.54 1.09-.29 2.44.59 3.35a2.91 2.91 0 0 " +
            "0 3.12.68l4.11 4.1c.18.19.45.19.63 0l1.04-1.03c.22-.18.22-.5.04-.64",
    )

    /** mdi:navigation */
    val Navigation: ImageVector = icon("Navigation", "M12 2L4.5 20.29l.71.71L12 18l6.79 3l.71-.71z")

    /** mdi:qrcode */
    val QrCode: ImageVector = icon(
        "QrCode",
        "M3 11h2v2H3zm8-6h2v4h-2zm-2 6h4v4h-2v-2H9zm6 0h2v2h2v-2h2v2h-2v2h2v4h-2v2h-2v-2h-4v2h-2v-4h4v-2h2v-2h-2zm4 8v-" +
            "4h-2v4zM15 3h6v6h-6zm2 2v2h2V5zM3 3h6v6H3zm2 2v2h2V5zM3 15h6v6H3zm2 2v2h2v-2z",
    )

    /** mdi:microphone */
    val Microphone: ImageVector = icon(
        "Microphone",
        "M12 2a3 3 0 0 1 3 3v6a3 3 0 0 1-3 3a3 3 0 0 1-3-3V5a3 3 0 0 1 3-3m7 9c0 3.53-2.61 6.44-6 6.93V21h-2v-3.07c-3.3" +
            "9-.49-6-3.4-6-6.93h2a5 5 0 0 0 5 5a5 5 0 0 0 5-5z",
    )

    /** mdi:gauge */
    val Gauge: ImageVector = icon(
        "Gauge",
        "M12 2A10 10 0 0 0 2 12a10 10 0 0 0 10 10a10 10 0 0 0 10-10A10 10 0 0 0 12 2m0 2a8 8 0 0 1 8 8c0 2.4-1 4.5-2.7 " +
            "6c-1.4-1.3-3.3-2-5.3-2s-3.8.7-5.3 2C5 16.5 4 14.4 4 12a8 8 0 0 1 8-8m2 1.89c-.38.01-.74.26-.9.65l-1.29 3.23l-." +
            "1.23c-.71.13-1.3.6-1.57 1.26c-.41 1.03.09 2.19 1.12 2.6s2.19-.09 2.6-1.12c.26-.66.14-1.42-.29-1.98l.1-.26l1.29" +
            "-3.21l.01-.03c.2-.51-.05-1.09-.56-1.3c-.13-.05-.26-.07-.41-.07M10 6a1 1 0 0 0-1 1a1 1 0 0 0 1 1a1 1 0 0 0 1-1a" +
            "1 1 0 0 0-1-1M7 9a1 1 0 0 0-1 1a1 1 0 0 0 1 1a1 1 0 0 0 1-1a1 1 0 0 0-1-1m10 0a1 1 0 0 0-1 1a1 1 0 0 0 1 1a1 1" +
            " 0 0 0 1-1a1 1 0 0 0-1-1",
    )

    /** mdi:volume-high */
    val VolumeHigh: ImageVector = icon(
        "VolumeHigh",
        "M14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.84-5 6.7v2.07c4-.91 7-4.49 7-8.77s-3-7.86-7-8.77M16.5 12c0-1.77-1-" +
            "3.29-2.5-4.03V16c1.5-.71 2.5-2.24 2.5-4M3 9v6h4l5 5V4L7 9z",
    )

    /** mdi:check-circle */
    val CheckCircle: ImageVector = icon(
        "CheckCircle",
        "M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10s10-4.5 10-10S17.5 2 12 2m-2 15l-5-5l1.41-1.41L10 14.17l7.59-7.59L19 8z",
    )

    /** mdi:alert */
    val Alert: ImageVector = icon("Alert", "M13 14h-2V9h2m0 9h-2v-2h2M1 21h22L12 2z")

    /** mdi:close */
    val Close: ImageVector = icon(
        "Close",
        "M19 6.41L17.59 5L12 10.59L6.41 5L5 6.41L10.59 12L5 17.59L6.41 19L12 13.41L17.59 19L19 17.59L13.41 12z",
    )

    /** mdi:chevron-down */
    val ChevronDown: ImageVector = icon("ChevronDown", "M7.41 8.58L12 13.17l4.59-4.59L18 10l-6 6l-6-6z")
}
