package com.salpim.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Asta Sans (42dot, OFL-1.1) — Pleos 시스템 서체. 라이선스: core/designsystem/FONT-LICENSE-OFL.txt */
val AstaSans = FontFamily(
    Font(R.font.asta_sans_regular, FontWeight.Normal),
    Font(R.font.asta_sans_medium, FontWeight.Medium),
    Font(R.font.asta_sans_bold, FontWeight.Bold),
    Font(R.font.asta_sans_extrabold, FontWeight.ExtraBold),
)

/**
 * 글자 크기 토큰 (Pleos Typography 크기/행간). 주행 화면은 24 이상만 쓴다 (docs/DESIGN.md).
 */
object SalpimType {
    private fun style(sizeSp: Int, lineSp: Int, weight: FontWeight) =
        TextStyle(fontFamily = AstaSans, fontSize = sizeSp.sp, lineHeight = lineSp.sp, fontWeight = weight)

    val Headline = style(60, 80, FontWeight.ExtraBold)
    val HeadlineSmall = style(44, 56, FontWeight.ExtraBold)
    val Title = style(40, 52, FontWeight.ExtraBold)
    val TitleSmall = style(32, 40, FontWeight.ExtraBold)
    val Body = style(28, 36, FontWeight.Normal)
    val BodyStrong = style(28, 36, FontWeight.Bold)
    val BodySmall = style(26, 34, FontWeight.Normal)
    val Label = style(24, 30, FontWeight.Normal)
    val LabelStrong = style(24, 30, FontWeight.Bold)

    /** 정차 화면 보조 문구 전용. 주행 화면에는 쓰지 않는다 */
    val Caption = style(20, 26, FontWeight.Normal)

    val material = Typography(
        headlineLarge = Headline,
        headlineMedium = HeadlineSmall,
        titleLarge = Title,
        titleMedium = TitleSmall,
        bodyLarge = Body,
        bodyMedium = BodySmall,
        labelLarge = Label,
        labelSmall = Caption,
    )
}
