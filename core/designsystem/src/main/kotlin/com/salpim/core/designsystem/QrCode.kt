package com.salpim.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * 휴대폰 카메라로 찍는 QR (ZXing core, Apache-2.0). 어두운 화면에서도 읽히도록 흰 바탕 + 여백(quiet zone).
 * 인코딩에 실패하면 아무것도 그리지 않는다 (주소 문자열은 옆에 따로 보여 준다).
 */
@Composable
fun QrCode(content: String, description: String, modifier: Modifier = Modifier, size: Dp = SalpimDimens.QrSize) {
    val matrix = remember(content) { encode(content) } ?: return
    Box(
        modifier
            .size(size)
            .background(Color.White)
            .padding(SalpimDimens.QrQuietZone)
            .semantics { contentDescription = description },
    ) {
        Canvas(Modifier.size(size - SalpimDimens.QrQuietZone * 2)) {
            val cell = this.size.width / matrix.width
            for (y in 0 until matrix.height) {
                for (x in 0 until matrix.width) {
                    if (matrix[x, y]) drawRect(Color.Black, Offset(x * cell, y * cell), Size(cell, cell))
                }
            }
        }
    }
}

// 인코딩 실패(내용이 너무 김 등)는 QR을 그리지 않는 것으로 충분하다. 주소는 글자로 따로 보여 준다
@Suppress("SwallowedException")
private fun encode(content: String): BitMatrix? = try {
    // 여백은 Compose 쪽에서 주므로 ZXing 여백은 0
    val hints = mapOf(EncodeHintType.MARGIN to 0, EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M)
    QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
} catch (e: WriterException) {
    null
}
