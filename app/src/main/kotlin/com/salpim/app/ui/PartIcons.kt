package com.salpim.app.ui

import androidx.compose.ui.graphics.vector.ImageVector
import com.salpim.core.designsystem.MdiIcons
import com.salpim.core.model.CarZone
import com.salpim.core.model.Explanation
import com.salpim.core.model.WarningCodes

/** 부위 → 아이콘. 계기판 경고등 모양 대신 부품을 뜻하는 아이콘 (docs/DESIGN.md) */
fun CarZone.icon(): ImageVector = when (this) {
    CarZone.ENGINE -> MdiIcons.Oil
    CarZone.BRAKE -> MdiIcons.BrakeFluid
    CarZone.TRANSMISSION -> MdiIcons.Transmission
    CarZone.WASHER -> MdiIcons.WasherFluid
    CarZone.SEAT_BELT -> MdiIcons.Seatbelt
    CarZone.AIRBAG -> MdiIcons.Airbag
}

/** 경고 → 아이콘. 같은 엔진 부위라도 엔진 이상은 오일통이 아니라 엔진으로, 모르는 경고는 느낌표 */
fun Explanation.icon(): ImageVector = when {
    code == WarningCodes.POWER_DOWN -> MdiIcons.Engine
    else -> zone?.icon() ?: MdiIcons.Alert
}
