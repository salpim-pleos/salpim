package com.salpim.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** 24 그리드 외곽선 아이콘. 실제 계기판 경고등과 똑같이 그리지 않는다 (docs/DESIGN.md) */
object SalpimIcons {
    private const val GRID = 24f
    private const val STROKE = 2f

    private fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
        ImageVector.Builder(name, GRID.dp, GRID.dp, GRID, GRID).apply(block).build()

    private fun ImageVector.Builder.stroked(pathData: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) =
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = STROKE,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = pathData,
        )

    val Mic: ImageVector = icon("Mic") {
        path(fill = SolidColor(Color.White)) {
            moveTo(9f, 3f)
            lineTo(15f, 3f)
            lineTo(15f, 14f)
            lineTo(9f, 14f)
            close()
        }
        stroked {
            moveTo(5.5f, 11f)
            arcToRelative(6.5f, 6.5f, 0f, false, false, 13f, 0f)
            moveTo(12f, 17.5f)
            lineTo(12f, 21f)
        }
    }

    val Speaker: ImageVector = icon("Speaker") {
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 9f)
            lineTo(4f, 15f)
            lineTo(8f, 15f)
            lineTo(13f, 19f)
            lineTo(13f, 5f)
            lineTo(8f, 9f)
            close()
        }
        stroked {
            moveTo(16f, 8.5f)
            arcToRelative(5f, 5f, 0f, false, true, 0f, 7f)
            moveTo(18.5f, 6f)
            arcToRelative(8.5f, 8.5f, 0f, false, true, 0f, 12f)
        }
    }

    val OilCan: ImageVector = icon("OilCan") {
        stroked {
            moveTo(2f, 10f)
            lineTo(14f, 10f)
            lineTo(19f, 6f)
            lineTo(22f, 6f)
            moveTo(2f, 10f)
            lineTo(2f, 18f)
            lineTo(15f, 18f)
            lineTo(19f, 13f)
            lineTo(19f, 10f)
            moveTo(7f, 10f)
            lineTo(7f, 7f)
            moveTo(5f, 7f)
            lineTo(9f, 7f)
        }
    }
}
